-- ============================================
-- 迁移: 07_disable_captcha.sql
-- 说明: 测试阶段关闭验证码，方便 Postman 调试
-- ============================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('验证码开关', 'sys.account.captchaEnabled', 'false', 'Y', 'admin', NOW(), '测试阶段关闭验证码')
ON DUPLICATE KEY UPDATE config_value = 'false', update_time = NOW();
