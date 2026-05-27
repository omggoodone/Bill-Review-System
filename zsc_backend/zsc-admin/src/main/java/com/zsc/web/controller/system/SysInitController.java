package com.zsc.web.controller.system;

import com.zsc.common.annotation.Anonymous;
import com.zsc.common.core.controller.BaseController;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.system.mapper.SysUserMapper;
import com.zsc.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统初始化控制器（无需登录）
 * 当系统无任何用户时，允许创建首个超级管理员
 */
@Anonymous
@RestController
@RequestMapping("/api/init")
public class SysInitController extends BaseController {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /**
     * 检查系统是否已初始化（是否存在任何用户）
     */
    @GetMapping("/status")
    public AjaxResult status() {
        Map<String, Object> data = new HashMap<>();
        data.put("initialized", userMapper.countUser() > 0);
        return success(data);
    }

    /**
     * 初始化系统：创建首个超级管理员账号
     */
    @PostMapping
    public AjaxResult init(@RequestBody Map<String, String> body) {
        if (userMapper.countUser() > 0) {
            return error("系统已初始化，无法重复操作");
        }
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return error("邮箱不能为空");
        }
        email = email.trim();

        String rawPassword = generatePassword();
        String username = "admin";

        SysUser user = new SysUser();
        user.setUserName(username);
        user.setNickName("超级管理员");
        user.setEmail(email);
        user.setPassword(SecurityUtils.encryptPassword(rawPassword));
        user.setStatus("0");
        user.setPwdUpdateDate(new Date());
        userMapper.insertUser(user);

        com.zsc.system.domain.SysUserRole ur = new com.zsc.system.domain.SysUserRole();
        ur.setUserId(user.getUserId());
        ur.setRoleId(1L);  // admin 角色
        userRoleMapper.batchUserRole(java.util.Collections.singletonList(ur));

        Map<String, Object> result = new HashMap<>();
        result.put("userName", username);
        result.put("password", rawPassword);
        return success(result);
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
