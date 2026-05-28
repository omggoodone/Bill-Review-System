package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.domain.dto.BizRegisterRequestDto;
import com.zsc.module.domain.entity.BizRegisterRequest;
import com.zsc.module.domain.vo.BizRegisterRequestVo;
import com.zsc.module.mapper.BizRegisterRequestMapper;
import com.zsc.module.service.BizRegisterRequestService;
import com.zsc.module.service.EmailService;
import com.zsc.system.mapper.SysUserMapper;
import com.zsc.system.mapper.SysUserRoleMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BizRegisterRequestServiceImpl extends ServiceImpl<BizRegisterRequestMapper, BizRegisterRequest>
        implements BizRegisterRequestService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Override
    public void submit(BizRegisterRequestDto dto) {
        checkEmailUnique(dto.getEmail());

        BizRegisterRequest req = new BizRegisterRequest();
        req.setEmail(dto.getEmail().trim());
        req.setNote(StringUtils.defaultString(dto.getNote()));
        req.setRoleKey(dto.getRoleKey());
        req.setStatus("0");
        req.setCreateTime(new Date());
        if (!this.save(req)) {
            throw new ServiceException("提交申请失败");
        }
    }

    @Override
    public List<BizRegisterRequestVo> listPending() {
        List<BizRegisterRequest> list = this.list(
            new LambdaQueryWrapper<BizRegisterRequest>()
                .eq(BizRegisterRequest::getStatus, "0")
                .orderByDesc(BizRegisterRequest::getCreateTime));
        return list.stream().map(r -> {
            BizRegisterRequestVo vo = new BizRegisterRequestVo();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public BizRegisterRequestVo approve(Long id, String comment) {
        BizRegisterRequest req = this.getById(id);
        if (req == null || !"0".equals(req.getStatus())) {
            throw new ServiceException("申请不存在或已处理");
        }

        String rawPassword = generatePassword();
        String username = generateUsername(req.getEmail());

        SysUser user = new SysUser();
        user.setUserName(username);
        user.setNickName(username);
        user.setEmail(req.getEmail());
        user.setPassword(SecurityUtils.encryptPassword(rawPassword));
        user.setStatus("0");
        user.setPwdUpdateDate(new Date());
        userMapper.insertUser(user);

        Long roleId = "reviewer".equals(req.getRoleKey()) ? 4L : 3L;
        userRoleMapper.deleteUserRoleByUserId(user.getUserId());
        com.zsc.system.domain.SysUserRole ur = new com.zsc.system.domain.SysUserRole();
        ur.setUserId(user.getUserId());
        ur.setRoleId(roleId);
        userRoleMapper.batchUserRole(java.util.Collections.singletonList(ur));

        String roleLabel = "reviewer".equals(req.getRoleKey()) ? "票据审核员" : "普通用户";
        emailService.sendCredentials(req.getEmail(), username, rawPassword, roleLabel);

        req.setStatus("1");
        req.setGeneratedUsername(username);
        req.setGeneratedPassword(rawPassword);
        req.setReviewBy(SecurityUtils.getUsername());
        req.setReviewTime(new Date());
        req.setReviewComment(comment);
        this.updateById(req);

        BizRegisterRequestVo vo = new BizRegisterRequestVo();
        BeanUtils.copyProperties(req, vo);
        vo.setGeneratedPassword(rawPassword);
        return vo;
    }

    @Override
    public void reject(Long id, String comment) {
        BizRegisterRequest req = this.getById(id);
        if (req == null || !"0".equals(req.getStatus())) {
            throw new ServiceException("申请不存在或已处理");
        }
        req.setStatus("2");
        req.setReviewBy(SecurityUtils.getUsername());
        req.setReviewTime(new Date());
        req.setReviewComment(comment);
        this.updateById(req);
    }

    private void checkEmailUnique(String email) {
        SysUser exist = userMapper.checkEmailUnique(email);
        if (exist != null) {
            throw new ServiceException("该邮箱已被注册");
        }
        long reqCount = this.count(
            new LambdaQueryWrapper<BizRegisterRequest>()
                .eq(BizRegisterRequest::getEmail, email)
                .eq(BizRegisterRequest::getStatus, "0"));
        if (reqCount > 0) {
            throw new ServiceException("该邮箱已有待审核申请");
        }
    }

    private String generateUsername(String email) {
        String prefix = email.substring(0, email.indexOf('@'))
            .replaceAll("[^a-zA-Z0-9]", "");
        if (prefix.isEmpty()) prefix = "user";

        String candidate = prefix;
        int tries = 0;
        while (tries < 20) {
            if (userMapper.selectUserByUserName(candidate) == null) return candidate;
            candidate = prefix + "_" + (1000 + RANDOM.nextInt(9000));
            tries++;
        }
        throw new ServiceException("无法生成唯一用户名");
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
