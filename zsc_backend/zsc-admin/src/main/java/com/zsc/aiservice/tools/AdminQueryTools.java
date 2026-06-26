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

    // ==================== Tool 4: 各类别金额汇总 ====================

    @Tool("获取各类别已通过票据的金额汇总，含占比。用于了解支出结构分布")
    public List<Map<String, Object>> getCategorySummary() {
        List<BizBill> approvedBills = billService.list(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "2"));

        if (approvedBills.isEmpty()) {
            return List.of(Map.of("info", "暂无已通过票据"));
        }

        // 按类别ID汇总
        Map<Long, Long> amountMap = new LinkedHashMap<>();
        for (BizBill b : approvedBills) {
            Long cid = b.getCategoryId();
            long amt = b.getAmount() != null ? b.getAmount().longValue() : 0L;
            amountMap.merge(cid != null ? cid : 0L, amt, Long::sum);
        }

        // 加载类别名称
        Map<Long, String> catNames = new HashMap<>();
        if (!amountMap.isEmpty()) {
            categoryService.listByIds(amountMap.keySet())
                    .forEach(c -> catNames.put(c.getCategoryId(), c.getCategoryName()));
        }

        long total = amountMap.values().stream().mapToLong(Long::longValue).sum();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Long> e : amountMap.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("categoryName", catNames.getOrDefault(e.getKey(), "未分类"));
            m.put("amount", e.getValue().toString());
            m.put("percent", total > 0 ?
                    String.format("%.1f%%", 100.0 * e.getValue() / total) : "0%");
            result.add(m);
        }
        result.sort((a, b) -> {
            long va = Long.parseLong((String) b.get("amount"));
            long vb = Long.parseLong((String) a.get("amount"));
            return Long.compare(va, vb);
        });
        return result;
    }

    // ==================== Tool 5: 月度提交趋势 ====================

    @Tool("获取本月票据提交的周度趋势，按自然周统计提交量")
    public List<Map<String, Object>> getMonthlyTrend() {
        List<com.zsc.module.domain.vo.TrendItemVo> trends = billService.getMonthlyTrend();
        return trends.stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("label", t.getLabel());
                    m.put("count", t.getCount());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ==================== Tool 6: 日报摘要 ====================

    @Tool("获取指定日期的运行日报：新提交数、审核通过数、审核退回数、退回率、" +
            "积压数量(超3天未审)和详情、当日审核员审核排行。" +
            "管理员问'日报'或'今天怎么样'时调用此工具")
    public Map<String, Object> getDailyDigest(
            @P("日期 yyyy-MM-dd，不传默认今天") String date) {

        LocalDate targetDate = (date != null && !date.isEmpty())
                ? LocalDate.parse(date, DATE_FMT) : LocalDate.now();

        Date dayStart = toDate(targetDate);
        Date dayEnd = toDate(targetDate.plusDays(1));

        // 当日提交
        long submitted = billService.count(new LambdaQueryWrapper<BizBill>()
                .between(BizBill::getCreateTime, dayStart, dayEnd));

        // 当日审核通过
        long approved = billService.count(new LambdaQueryWrapper<BizBill>()
                .eq(BizBill::getStatus, "2")
                .between(BizBill::getAuditTime, dayStart, dayEnd));

        // 当日退回
        long rejected = billService.count(new LambdaQueryWrapper<BizBill>()
                .eq(BizBill::getStatus, "3")
                .between(BizBill::getAuditTime, dayStart, dayEnd));

        long audited = approved + rejected;
        String rejectionRate = audited > 0 ?
                String.format("%.1f%%", 100.0 * rejected / audited) : "N/A";

        // 积压（待审超3天）
        Date threeDaysAgo = toDate(targetDate.minusDays(3));
        List<BizBill> staleBills = billService.list(new LambdaQueryWrapper<BizBill>()
                .eq(BizBill::getStatus, "1")
                .lt(BizBill::getCreateTime, threeDaysAgo)
                .orderByAsc(BizBill::getCreateTime));

        List<Map<String, String>> staleInfo = staleBills.stream()
                .map(b -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("billNo", b.getBillNo());
                    m.put("title", b.getTitle());
                    m.put("createBy", b.getCreateBy());
                    m.put("createTime", fmt(b.getCreateTime()));
                    return m;
                })
                .collect(Collectors.toList());

        // 当日审核员排行
        List<BizBill> todayAudited = billService.list(new LambdaQueryWrapper<BizBill>()
                .in(BizBill::getStatus, "2", "3")
                .between(BizBill::getAuditTime, dayStart, dayEnd));

        Map<String, Long> reviewerCount = new LinkedHashMap<>();
        for (BizBill b : todayAudited) {
            String name = b.getAuditBy();
            if (name != null) reviewerCount.merge(name, 1L, Long::sum);
        }
        List<Map<String, Object>> ranking = reviewerCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> Map.<String, Object>of("审核员", e.getKey(), "审核数", e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> digest = new LinkedHashMap<>();
        digest.put("日期", targetDate.format(DATE_FMT));
        digest.put("新提交数", submitted);
        digest.put("审核通过数", approved);
        digest.put("审核退回数", rejected);
        digest.put("退回率", rejectionRate);
        digest.put("积压数(超3天)", staleBills.size());
        digest.put("积压详情", staleInfo);
        digest.put("当日审核排行", ranking);
        return digest;
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
