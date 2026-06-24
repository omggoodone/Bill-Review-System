package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.tools.UserAccountUtils;
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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizRegisterRequestServiceImpl extends ServiceImpl<BizRegisterRequestMapper, BizRegisterRequest>
        implements BizRegisterRequestService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private com.zsc.system.mapper.SysRoleMapper roleMapper;

    @Autowired
    private EmailService emailService;


    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public BizRegisterRequestVo approve(Long id, String comment) {
        BizRegisterRequest req = this.getById(id);
        if (req == null || !"0".equals(req.getStatus())) {
            throw new ServiceException("申请不存在或已处理");
        }

        String rawPassword = UserAccountUtils.generatePassword();
        String username = UserAccountUtils.generateUsername(req.getEmail(), "user", userMapper);

        SysUser user = new SysUser();
        user.setUserName(username);
        user.setNickName(username);
        user.setEmail(req.getEmail());
        user.setPassword(SecurityUtils.encryptPassword(rawPassword));
        user.setStatus("0");
        user.setPwdUpdateDate(new Date());
        userMapper.insertUser(user);

        Long roleId = getRoleIdByKey(req.getRoleKey());
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
    @Transactional(rollbackFor = Exception.class)
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

    private Long getRoleIdByKey(String roleKey) {
        com.zsc.common.core.domain.entity.SysRole role = roleMapper.checkRoleKeyUnique(roleKey);
        if (role == null) {
            throw new ServiceException("系统错误：角色[" + roleKey + "]不存在");
        }
        return role.getRoleId();
    }
}
