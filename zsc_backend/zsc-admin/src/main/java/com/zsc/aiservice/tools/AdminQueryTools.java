package com.zsc.aiservice.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsc.common.core.domain.entity.SysRole;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.module.domain.entity.BizAuditLog;
import com.zsc.module.domain.entity.BizBill;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.domain.vo.ReviewerWorkloadVo;
import com.zsc.module.mapper.BizAuditLogMapper;
import com.zsc.module.service.BizBillService;
import com.zsc.module.service.BizCategoryService;
import com.zsc.system.mapper.SysRoleMapper;
import com.zsc.system.mapper.SysUserMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员查询工具集
 * 暴露给 AI 的 10 个 @Tool 方法，全部只读，复用已有 Service/Mapper
 */
@Component
public class AdminQueryTools {

    @Autowired
    private BizBillService billService;

    @Autowired
    private BizCategoryService categoryService;

    @Autowired
    private BizAuditLogMapper auditLogMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== Tool 1: 系统全景统计 ====================

    @Tool("获取系统全景统计：用户总数、各角色人数、票据总量及各状态数量。用于管理员了解系统整体运行状况")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("总用户数", userMapper.countUser());
        stats.put("超级管理员数", userMapper.countUserByRoleKey("admin"));
        stats.put("系统管理员数", userMapper.countUserByRoleKey("admin_user"));
        stats.put("审核员数", userMapper.countUserByRoleKey("reviewer"));
        stats.put("普通用户数", userMapper.countUserByRoleKey("user"));
        stats.put("票据总量", billService.count());
        stats.put("待审核数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "1")));
        stats.put("已通过数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "2")));
        stats.put("已退回数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "3")));
        stats.put("草稿数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "0")));
        return stats;
    }
}
