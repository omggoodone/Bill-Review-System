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

    // ==================== Tool 2: 审核员工作量排行 ====================

    @Tool("获取审核员工作量排行。返回已格式化的 Markdown 表格文本。")
    public String getReviewerWorkload() {
        List<BizBill> bills = billService.list(
                new LambdaQueryWrapper<BizBill>()
                        .isNotNull(BizBill::getAuditBy)
                        .in(BizBill::getStatus, "2", "3"));

        Map<String, ReviewerWorkloadVo> map = new LinkedHashMap<>();
        for (BizBill b : bills) {
            String name = b.getAuditBy();
            ReviewerWorkloadVo vo = map.computeIfAbsent(name,
                    k -> new ReviewerWorkloadVo(k, 0, 0, 0));
            vo.setTotalCount(vo.getTotalCount() + 1);
            if ("2".equals(b.getStatus())) {
                vo.setApprovedCount(vo.getApprovedCount() + 1);
            } else {
                vo.setRejectedCount(vo.getRejectedCount() + 1);
            }
        }

        List<ReviewerWorkloadVo> sorted = map.values().stream()
                .sorted(Comparator.comparingLong(ReviewerWorkloadVo::getTotalCount).reversed())
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            return "暂无审核记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共 **").append(sorted.size()).append("** 位审核员参与审核。\n\n");
        sb.append("| 审核员 | 审核总量 | 通过数 | 退回数 | 通过率 |\n");
        sb.append("|--------|----------|--------|--------|--------|\n");
        for (ReviewerWorkloadVo w : sorted) {
            long total = w.getApprovedCount() + w.getRejectedCount();
            String rate = total > 0
                    ? String.format("%.1f%%", 100.0 * w.getApprovedCount() / total) : "0%";
            sb.append("| ").append(w.getReviewerName())
                    .append(" | ").append(w.getTotalCount())
                    .append(" | ").append(w.getApprovedCount())
                    .append(" | ").append(w.getRejectedCount())
                    .append(" | ").append(rate)
                    .append(" |\n");
        }

        return sb.toString();
    }

    // ==================== Tool 3: 多条件搜索票据 ====================

    @Tool("按条件搜索票据：支持按状态、类别ID、提交人、审核人、关键词(标题/编号)、日期范围组合查询。" +
            "所有参数均可选，AI根据用户意图传入相关参数")
    public List<Map<String, Object>> searchBills(
            @P("状态：0草稿 1待审核 2已通过 3已退回") String status,
            @P("类别ID") Long categoryId,
            @P("提交人用户名") String createBy,
            @P("审核人用户名") String auditBy,
            @P("搜索关键词，匹配标题和票据编号") String keyword,
            @P("起始日期 yyyy-MM-dd") String startDate,
            @P("截止日期 yyyy-MM-dd") String endDate) {

        LambdaQueryWrapper<BizBill> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq(BizBill::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(BizBill::getCategoryId, categoryId);
        }
        if (createBy != null && !createBy.isEmpty()) {
            wrapper.eq(BizBill::getCreateBy, createBy);
        }
        if (auditBy != null && !auditBy.isEmpty()) {
            wrapper.eq(BizBill::getAuditBy, auditBy);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(BizBill::getTitle, keyword)
                    .or()
                    .like(BizBill::getBillNo, keyword));
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(BizBill::getCreateTime, parseDateStart(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(BizBill::getCreateTime, parseDateEnd(endDate));
        }

        wrapper.orderByDesc(BizBill::getCreateTime);
        // 限制最多返回30条，避免单次返回过多
        wrapper.last("LIMIT 30");

        List<BizBill> bills = billService.list(wrapper);

        if (bills.isEmpty()) {
            return List.of(Map.of("info", "未找到符合条件的票据"));
        }

        // 批量加载类别名称
        Set<Long> catIds = bills.stream()
                .map(BizBill::getCategoryId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> catNameMap = new HashMap<>();
        if (!catIds.isEmpty()) {
            categoryService.listByIds(catIds)
                    .forEach(c -> catNameMap.put(c.getCategoryId(), c.getCategoryName()));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (BizBill bill : bills) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", bill.getId());
            m.put("billNo", bill.getBillNo());
            m.put("title", bill.getTitle());
            m.put("amount", bill.getAmount() != null ? bill.getAmount().toString() : "0.00");
            m.put("status", statusLabel(bill.getStatus()));
            m.put("categoryName", catNameMap.getOrDefault(bill.getCategoryId(), "未知"));
            m.put("createBy", bill.getCreateBy());
            m.put("createTime", fmt(bill.getCreateTime()));
            m.put("auditBy", bill.getAuditBy());
            m.put("auditComment", bill.getAuditComment());
            result.add(m);
        }
        return result;
    }


    // ==================== Markdown 表格格式化 ====================

    /**
     * 将结构化数据转为 Markdown 管道表格字符串。
     * LLM 拿到后直接用，无需自己排版。
     */
    private String toMarkdownTable(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        // 表头
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        // 分隔行
        sb.append("| ").append(headers.stream().map(h -> "---").collect(Collectors.joining(" | "))).append(" |\n");
        // 数据行
        for (List<String> row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }
        return sb.toString();
    }

    // ==================== 辅助方法 ====================

    private String statusLabel(String s) {
        if (s == null) return "未知";
        return switch (s) {
            case "0" -> "草稿";
            case "1" -> "待审核";
            case "2" -> "已通过";
            case "3" -> "已退回";
            default -> "未知(" + s + ")";
        };
    }

    private String fmt(Date date) {
        if (date == null) return null;
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date parseDateStart(String dateStr) {
        return toDate(LocalDate.parse(dateStr, DATE_FMT));
    }

    private Date parseDateEnd(String dateStr) {
        return toDate(LocalDate.parse(dateStr, DATE_FMT).plusDays(1));
    }

    private long countSubmittedBetween(Date start, Date end) {
        return billService.count(new LambdaQueryWrapper<BizBill>()
                .between(BizBill::getCreateTime, start, end));
    }

    private long countAuditedBetween(String status, Date start, Date end) {
        return billService.count(new LambdaQueryWrapper<BizBill>()
                .eq(BizBill::getStatus, status)
                .between(BizBill::getAuditTime, start, end));
    }

    private String calcChange(long current, long previous) {
        if (previous == 0) return current > 0 ? "新增（上周为0）" : "持平（均为0）";
        double change = 100.0 * (current - previous) / previous;
        String sign = change >= 0 ? "+" : "";
        return sign + String.format("%.1f%%", change);
    }
}
