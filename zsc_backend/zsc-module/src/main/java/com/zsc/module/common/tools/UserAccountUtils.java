package com.zsc.module.common.tools;

import com.zsc.module.common.exception.ServiceException;
import com.zsc.system.mapper.SysUserMapper;

import java.security.SecureRandom;

/**
 * 用户账号生成工具：随机密码 + 基于邮箱的用户名生成
 */
public class UserAccountUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /**
     * 生成 8 位随机密码（不含易混淆字符）
     */
    public static String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 基于邮箱前缀生成唯一用户名
     *
     * @param email         邮箱地址
     * @param defaultPrefix 前缀提取失败时的默认值
     * @param userMapper    用于校验用户名唯一性
     * @return 唯一用户名
     */
    public static String generateUsername(String email, String defaultPrefix, SysUserMapper userMapper) {
        String prefix = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9]", "");
        if (prefix.isEmpty()) {
            prefix = defaultPrefix;
        }

        String candidate = prefix;
        int tries = 0;
        while (tries < 20) {
            if (userMapper.selectUserByUserName(candidate) == null) {
                return candidate;
            }
            candidate = prefix + "_" + (1000 + RANDOM.nextInt(9000));
            tries++;
        }
        throw new ServiceException("无法生成唯一用户名");
    }
}
