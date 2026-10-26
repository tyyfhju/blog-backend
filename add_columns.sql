-- ============================================
-- 手动添加欢迎语和技术栈列（MySQL 5.7 兼容）
-- 如果报错 Duplicate column 说明列已存在，忽略即可
-- ============================================
ALTER TABLE `blog_setting` ADD COLUMN `welcome_text` TEXT COMMENT '欢迎语';
ALTER TABLE `blog_setting` ADD COLUMN `tech_stack` TEXT COMMENT '技术栈';
