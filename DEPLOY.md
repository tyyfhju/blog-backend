# Blog 部署教程（Ubuntu 22.04）

> 工具：Xterminal 连服务器、传文件；Navicat 连数据库、导数据。
> 结构：Nginx 静态前端 + Spring Boot 后端 + MySQL 8 + Redis + 自动续期 HTTPS。
> 只保留必要步骤，排查命令放在最后。

## 0. 准备

- 服务器：Ubuntu 22.04 LTS，2 核 2G 以上
- 域名：A 记录指向服务器 IP（有 www 就再加一条）
- 安全组/防火墙：只放行 22、80、443，不要开放 3306、6379
- 本地已经能跑起来：
  - 前端源码目录，打包后是 `dist`
  - 后端源码目录，打包后是 `target/blog-0.0.1-SNAPSHOT.jar`
  - 后端项目的 `src/main/resources/application.yml`
  - 本地 MySQL 里有 `blog` 库

下文用到的占位符，部署时换成自己的：

| 占位符 | 含义 |
|---|---|
| `<服务器IP>` | 公网 IP |
| `<域名>` | 例如 `blog.example.com` |
| `<邮箱>` | 申请证书用的邮箱 |
| `<数据库密码>` | 给 `blog` 账号设置的密码 |
| `<Redis密码>` | Redis 密码 |

> 中文域名在 Nginx 和 certbot 里用 punycode 形式（`xn--` 开头），不要直接写汉字。

> 如果正式域名不是代码里写死的那个，先看第 11 节，改完再打包。

---

## 1. Xterminal 装环境

Xterminal 新建 SSH 连接，登录服务器，整段执行：

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-8-jdk mysql-server redis-server nginx certbot python3-certbot-nginx unzip

# 如果提示找不到 openjdk-8-jdk：
# sudo add-apt-repository universe && sudo apt update

# 确认 Java 是 1.8
java -version

# 时区
sudo timedatectl set-timezone Asia/Shanghai

sudo systemctl enable --now mysql redis-server nginx
```

如果 `java -version` 不是 1.8：

```bash
sudo update-alternatives --config java
```

选 `/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java`。

2G 内存的机器建议加 2G swap（已有可跳过）：

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## 2. 建数据库账号（Xterminal）

```bash
sudo mysql <<'SQL'
CREATE DATABASE IF NOT EXISTS blog
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'blog'@'localhost' IDENTIFIED BY '<数据库密码>';
ALTER USER 'blog'@'localhost' IDENTIFIED BY '<数据库密码>';
GRANT ALL PRIVILEGES ON blog.* TO 'blog'@'localhost';
FLUSH PRIVILEGES;
SQL
```

测试一下：

```bash
mysql -u blog -p -e "SHOW DATABASES;" 
```

输入 `<数据库密码>`，能看到 `blog` 就行。

---

## 3. Navicat 导数据

### 3.1 连服务器

Navicat → 连接 → MySQL：

- **常规**：主机 `127.0.0.1`，端口 `3306`，用户 `blog`，密码 `<数据库密码>`，数据库 `blog`
- **SSH**：勾选“使用 SSH 隧道”
  - 主机：`<服务器IP>`
  - 端口：`22`
  - 用户名：你的 SSH 用户（一般是 `root`）
  - 验证方式：密码或密钥

点“测试连接”。成功后不用在安全组开放 3306，Navicat 走的是 SSH 隧道。

### 3.2 本地库传到服务器

Navicat 里同时有“本地 blog”和“服务器 blog”两个连接，然后：

**工具 → 数据传输**

- 源：本地 `blog`
- 目标：服务器 `blog`
- 勾选所有表，传输“结构 + 数据”
- 开始

这是新库，`user`、`article`、`category`、`tag`、`photo` 等表可以全传，后台管理员账号也会一起过去。

如果目标库已经有数据，改用“工具 → 数据同步”，只同步有变化的表。

传完在服务器连接里刷新，确认表和数据都在。

> 建议传之前先在本地库右键“转储 SQL 文件”备份一份。

---

## 4. Redis 密码

```bash
sudo sed -i 's/^# requirepass .*/requirepass <Redis密码>/' /etc/redis/redis.conf
sudo sed -i 's/^requirepass .*/requirepass <Redis密码>/' /etc/redis/redis.conf
sudo systemctl restart redis-server

redis-cli --no-auth-warning -a '<Redis密码>' ping
```

返回 `PONG` 即可。Redis 默认只监听 `127.0.0.1`，不用改。

---

## 5. 后端

### 5.1 本地打包

```bash
cd 后端项目目录
mvn clean package -DskipTests
```

产物：`target/blog-0.0.1-SNAPSHOT.jar`。

### 5.2 建目录

```bash
sudo mkdir -p /opt/blog
sudo useradd --system --home /opt/blog --shell /usr/sbin/nologin blog 2>/dev/null || true
sudo chown -R blog:blog /opt/blog
```

### 5.3 Xterminal 上传

打开 Xterminal 的文件管理（SFTP），上传两个文件：

- 本地 `target/blog-0.0.1-SNAPSHOT.jar` → 服务器 `/opt/blog/blog.jar`
- 本地 `src/main/resources/application.yml` → 服务器 `/opt/blog/application.yml`

然后：

```bash
sudo chown blog:blog /opt/blog/blog.jar /opt/blog/application.yml
sudo chmod 600 /opt/blog/application.yml
```

### 5.4 改 application.yml

用 Xterminal 的编辑器打开 `/opt/blog/application.yml`，改这几处：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8
    username: blog
    password: <数据库密码>

  redis:
    host: localhost
    port: 6379
    password: <Redis密码>
```

其他 `mail`、`cos`、`app.jwt.secret` 保持本地能用的值，不要外传。

> 密码里有 `#`、`:`、`@`、`&` 这类字符时，要用双引号包起来，例如 `password: "abc#123"`。不包的话 `#` 后面会被 YAML 当成注释。

关键点：MySQL 8 的 JDBC URL 要加 `allowPublicKeyRetrieval=true`，否则可能报 `Public Key Retrieval is not allowed`。

### 5.5 systemd 启动

```bash
sudo tee /etc/systemd/system/blog.service > /dev/null <<'EOF'
[Unit]
Description=Blog Backend
After=network.target mysql.service redis-server.service

[Service]
User=blog
Group=blog
WorkingDirectory=/opt/blog
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/blog/blog.jar --spring.config.location=file:/opt/blog/application.yml
SuccessExitStatus=143
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now blog
sudo systemctl status blog --no-pager
```

看接口通不通：

```bash
curl -s http://127.0.0.1:8080/api/blog-setting | head -c 300
```

有 JSON 返回就说明后端、数据库都通了。报错看：

```bash
sudo journalctl -u blog -n 100 --no-pager
```

---

## 6. 前端

### 6.1 本地打包

```bash
cd 前端项目目录
npm run build
```

产物在 `dist` 目录。

### 6.2 Xterminal 上传

```bash
sudo mkdir -p /var/www/blog/dist
```

用 Xterminal 文件管理，把本地 `dist` **里面的内容**上传到 `/var/www/blog/dist`：

- `index.html`
- `js/`
- `css/`
- `fonts/`
- `img/`
- `favicon.ico`

注意不要传成 `/var/www/blog/dist/dist`。

```bash
sudo chown -R www-data:www-data /var/www/blog
sudo find /var/www/blog -type d -exec chmod 755 {} \;
sudo find /var/www/blog -type f -exec chmod 644 {} \;
```

---

## 7. Nginx

```bash
sudo tee /etc/nginx/sites-available/blog > /dev/null <<'EOF'
server {
    listen 80;
    listen [::]:80;
    server_name <域名> www.<域名>;

    root /var/www/blog/dist;
    index index.html;
    client_max_body_size 50m;

    location ^~ /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location = /sitemap.xml {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
```

没有 www 域名的话，把 `server_name` 里的 `www.<域名>` 删掉。

启用：

```bash
sudo ln -sf /etc/nginx/sites-available/blog /etc/nginx/sites-enabled/blog
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

浏览器打开 `http://<域名>`，再试 `http://<域名>/api/blog-setting`。都能打开再继续。

---

## 8. HTTPS 自动续期

```bash
sudo certbot --nginx -d <域名> -d www.<域名> \
  --redirect --agree-tos -m <邮箱> --no-eff-email
```

没有 www 就只保留 `-d <域名>`。

确认自动续期：

```bash
systemctl list-timers | grep certbot
sudo certbot renew --dry-run
```

Ubuntu 用 apt 装的 certbot 一般自带 `certbot.timer`。如果没有：

```bash
sudo systemctl enable --now certbot.timer
```

`renew --dry-run` 成功就说明到期会自动续，不用手工管。

---

## 9. 防火墙

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
sudo ufw status
```

云服务器的安全组也要放行 22、80、443。3306、6379 不要对公网开放。

---

## 10. 上线检查

- `https://<域名>` 首页正常
- 文章列表、文章详情、关于页、相册
- 后台登录
- 文章编辑（带 ```nginx 代码块的文章能正常打开）
- 后台上传图片
- 手机宽度下页面不横向滚动
- `https://<域名>/sitemap.xml` 能打开
- 证书：`sudo certbot renew --dry-run` 通过
- 日志：`sudo journalctl -u blog -n 100 --no-pager` 没有 ERROR

---

## 11. 换域名要改代码

后端有两处写死了域名：

- `src/main/java/com/zjy/blog/controller/SitemapController.java` 的 `BASE_URL`
- `src/main/java/com/zjy/blog/config/SecurityConfig.java` 的 CORS 白名单

如果正式域名和代码里的一致，跳过这节。不一致就改成新域名，重新 `mvn clean package -DskipTests`，再按第 5 节上传 jar、重启：

```bash
sudo systemctl restart blog
```

---

## 12. 常见问题

**502 Bad Gateway**

后端没起来。依次看：

```bash
sudo systemctl status blog --no-pager
sudo journalctl -u blog -n 100 --no-pager
ss -lntp | grep 8080
```

多数是数据库密码、Redis 密码不对，或者 MySQL 没启动。

**刷新文章页 404**

Nginx 少了 `try_files $uri $uri/ /index.html;`，检查第 7 节配置。

**Public Key Retrieval is not allowed**

JDBC URL 加 `allowPublicKeyRetrieval=true`，改完重启后端。

**Access denied for user 'blog'**

先在服务器上试：

```bash
mysql -u blog -p blog
```

能进说明密码对，问题在 `application.yml`；进不去就重新执行第 2 节的授权。

**上传图片 413**

Nginx 的 `client_max_body_size` 调到 `50m`，然后 `sudo systemctl reload nginx`。后端 `spring.servlet.multipart` 也保持 50MB。

**图片上传失败**

检查 `application.yml` 里的 `cos.secret-id`、`secret-key`、`bucket-name`、`region` 是否是正式环境的值。

**Certbot 申请失败**

- 域名 A 记录还没生效：`ping <域名>` 看是不是服务器 IP
- 80 端口没放行
- Cloudflare 开了代理：申请时先改成“仅 DNS”，证书下来再开代理

**后台接口 404**

确认 Nginx 里是 `location ^~ /api/`，`proxy_pass http://127.0.0.1:8080;` 后面不要带路径。
