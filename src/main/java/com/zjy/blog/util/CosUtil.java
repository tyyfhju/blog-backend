package com.zjy.blog.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CosUtil {

    @Value("${cos.secret-id}")
    private String secretId;

    @Value("${cos.secret-key}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucket-name}")
    private String bucketName;

    @Value("${cos.base-url}")
    private String baseUrl;

    private COSClient getClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        return new COSClient(cred, config);
    }

    public String upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String rawName = System.currentTimeMillis() + "_" + originalName.replaceAll("[^a-zA-Z0-9.\\-_\\u4e00-\\u9fa5]", "_");
        String safeName = URLEncoder.encode(rawName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        String key = "blog/" + safeName;

        File tempFile = File.createTempFile("cos-", ext);
        file.transferTo(tempFile);

        COSClient client = getClient();
        client.putObject(new PutObjectRequest(bucketName, key, tempFile));
        client.shutdown();
        tempFile.delete();

        return key;
    }

    public byte[] download(String key) throws IOException {
        COSClient client = getClient();
        try {
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            COSObject cosObject = client.getObject(request);
            InputStream input = cosObject.getObjectContent();
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int n;
                while ((n = input.read(data)) != -1) {
                    buffer.write(data, 0, n);
                }
                return buffer.toByteArray();
            } finally {
                input.close();
            }
        } finally {
            client.shutdown();
        }
    }

    public void delete(String key) {
        COSClient client = getClient();
        try {
            client.deleteObject(bucketName, key);
            System.out.println("COS 删除成功: key=" + key);
        } catch (Exception e) {
            System.err.println("COS 删除失败: key=" + key + ", error=" + e.getMessage());
            e.printStackTrace();
        } finally {
            client.shutdown();
        }
    }

    private String extractKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("/api/files/")) {
            return "blog/" + url.substring(url.lastIndexOf("/") + 1);
        }
        return url;
    }

    public void deleteByUrl(String url) {
        String key = extractKeyFromUrl(url);
        if (key != null) {
            delete(key);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}