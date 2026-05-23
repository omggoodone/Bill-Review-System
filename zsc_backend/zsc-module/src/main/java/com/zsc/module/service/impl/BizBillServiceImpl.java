package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.common.utils.SecurityUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.BizBillDto;
import com.zsc.module.domain.dto.BizBillReviewDto;
import com.zsc.module.domain.dto.query.BizBillQueryDto;
import com.zsc.module.domain.entity.BizAuditLog;
import com.zsc.module.domain.entity.BizBill;
import com.zsc.module.domain.entity.BizBillFile;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.domain.vo.BizAuditLogVo;
import com.zsc.module.domain.vo.BizBillDetailVo;
import com.zsc.module.domain.vo.BizBillFileVo;
import com.zsc.module.domain.vo.BizBillVo;
import com.zsc.module.mapper.BizAuditLogMapper;
import com.zsc.module.mapper.BizBillFileMapper;
import com.zsc.module.mapper.BizBillMapper;
import com.zsc.module.mapper.BizCategoryMapper;
import com.zsc.module.service.BizBillService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 票据表 服务实现类
 */
@Service
@Transactional
public class BizBillServiceImpl extends ServiceImpl<BizBillMapper, BizBill> implements BizBillService {

    @Autowired
    private BizBillFileMapper fileMapper;

    @Autowired
    private BizAuditLogMapper auditLogMapper;

    @Autowired
    private BizCategoryMapper categoryMapper;

    /**
     * 管理员禁止执行用户专属操作
     */
    private void checkNotAdmin() {
        if (SecurityUtils.hasPermi("biz:bill:review")) {
            throw new ServiceException("管理员不能执行用户操作！");
        }
    }

    // ==================== 新增 ====================

    @Override
    public void addBill(BizBillDto dto) {
        checkNotAdmin();
        BizBill bill = new BizBill();
        BeanUtils.copyProperties(dto, bill);

        bill.setCreateBy(SecurityUtils.getUsername());
        bill.setCreateTime(new Date());

        // 草稿生成临时编号，防止 NOT NULL 约束报错；提交时替换为正式编号
        if ("1".equals(dto.getStatus())) {
            bill.setBillNo(generateBillNo());
        } else {
            bill.setBillNo("DRAFT-" + System.currentTimeMillis());
        }

        if (!this.save(bill)) {
            throw new ServiceException("系统错误，票据保存失败！");
        }

        // 保存附件记录
        saveAttachments(bill.getId(), dto.getAttachment());
    }

    // ==================== 查询列表 ====================

    @Override
    public PageResult<BizBillVo> queryBills(BizBillQueryDto dto) {
        LambdaQueryWrapper<BizBill> wrapper = new LambdaQueryWrapper<>();

        // 普通用户只查自己的票据
        if (!SecurityUtils.hasPermi("biz:bill:review")) {
            wrapper.eq(BizBill::getCreateBy, SecurityUtils.getUsername());
        }

        // 条件过滤
        wrapper.like(StringUtils.isNotBlank(dto.getKeywords()), BizBill::getTitle, dto.getKeywords())
               .eq(dto.getCategoryId() != null, BizBill::getCategoryId, dto.getCategoryId())
               .eq(StringUtils.isNotBlank(dto.getStatus()), BizBill::getStatus, dto.getStatus())
               .ge(StringUtils.isNotBlank(dto.getStartTime()), BizBill::getCreateTime, dto.getStartTime())
               .le(StringUtils.isNotBlank(dto.getEndTime()), BizBill::getCreateTime, dto.getEndTime())
               .orderByDesc(BizBill::getCreateTime);

        Page<BizBill> page = this.page(dto.convertToPage(), wrapper);

        // 批量查类别名称
        Map<Long, String> categoryNameMap = buildCategoryNameMap(page.getRecords());

        // Entity → VO
        List<BizBillVo> voList = page.getRecords().stream().map(bill -> {
            BizBillVo vo = new BizBillVo();
            BeanUtils.copyProperties(bill, vo);
            if (bill.getCategoryId() != null) {
                vo.setCategoryName(categoryNameMap.get(bill.getCategoryId()));
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult result = PageResult.fromPage(page);
        result.setList(voList);
        return result;
    }

    // ==================== 详情 ====================

    @Override
    public BizBillDetailVo getBillDetail(Long id) {
        BizBill bill = this.getById(id);
        if (bill == null) {
            throw new ServiceException("票据不存在！");
        }

        // 非管理员只能查看自己的票据
        if (!SecurityUtils.hasPermi("biz:bill:review")
                && !SecurityUtils.getUsername().equals(bill.getCreateBy())) {
            throw new ServiceException("只能查看自己的票据！");
        }

        BizBillDetailVo vo = new BizBillDetailVo();
        BeanUtils.copyProperties(bill, vo);

        // 查类别名称
        if (bill.getCategoryId() != null) {
            BizCategory category = categoryMapper.selectById(bill.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        // 查附件列表
        List<BizBillFile> files = fileMapper.selectList(
            new LambdaQueryWrapper<BizBillFile>()
                .eq(BizBillFile::getBillId, id)
                .orderByAsc(BizBillFile::getSortOrder)
        );
        vo.setFiles(files.stream().map(f -> {
            BizBillFileVo fvo = new BizBillFileVo();
            BeanUtils.copyProperties(f, fvo);
            return fvo;
        }).collect(Collectors.toList()));

        // 查审核历史
        List<BizAuditLog> logs = auditLogMapper.selectList(
            new LambdaQueryWrapper<BizAuditLog>()
                .eq(BizAuditLog::getBillId, id)
                .orderByDesc(BizAuditLog::getAuditTime)
        );
        vo.setAuditLogs(logs.stream().map(l -> {
            BizAuditLogVo lvo = new BizAuditLogVo();
            BeanUtils.copyProperties(l, lvo);
            return lvo;
        }).collect(Collectors.toList()));

        return vo;
    }

    // ==================== 编辑 ====================

    @Override
    public void updateBill(BizBillDto dto) {
        checkNotAdmin();
        BizBill bill = this.getById(dto.getId());
        if (bill == null) {
            throw new ServiceException("票据不存在！");
        }

        // 仅草稿或已退回可编辑
        if (!List.of("0", "3").contains(bill.getStatus())) {
            throw new ServiceException("只能编辑草稿或已退回状态的票据！");
        }

        // 校验数据归属
        if (!SecurityUtils.getUsername().equals(bill.getCreateBy())) {
            throw new ServiceException("只能编辑自己的票据！");
        }

        // 保留字段不被 null 覆盖
        if (StringUtils.isNotBlank(dto.getAttachment())) {
            bill.setAttachment(dto.getAttachment());
        }

        bill.setTitle(dto.getTitle());
        bill.setCategoryId(dto.getCategoryId());
        bill.setAmount(dto.getAmount());
        bill.setDescription(dto.getDescription());
        bill.setUpdateBy(SecurityUtils.getUsername());
        bill.setUpdateTime(new Date());

        if (!this.updateById(bill)) {
            throw new ServiceException("系统错误，票据更新失败！");
        }

        // 仅当传入了新附件时才替换旧附件
        if (StringUtils.isNotBlank(dto.getAttachment())) {
            fileMapper.delete(new LambdaQueryWrapper<BizBillFile>()
                .eq(BizBillFile::getBillId, bill.getId()));
            saveAttachments(bill.getId(), dto.getAttachment());
        }
    }

    // ==================== 提交审核 ====================

    @Override
    public void submitBill(Long id) {
        checkNotAdmin();
        BizBill bill = this.getById(id);
        if (bill == null) {
            throw new ServiceException("票据不存在！");
        }

        // 仅草稿或已退回可提交
        if (!List.of("0", "3").contains(bill.getStatus())) {
            throw new ServiceException("只能提交草稿或已退回状态的票据！");
        }

        // 校验数据归属
        if (!SecurityUtils.getUsername().equals(bill.getCreateBy())) {
            throw new ServiceException("只能提交自己的票据！");
        }

        // 草稿有临时编号的，替换为正式编号
        if (StringUtils.isBlank(bill.getBillNo()) || bill.getBillNo().startsWith("DRAFT-")) {
            bill.setBillNo(generateBillNo());
        }

        bill.setStatus("1");
        bill.setUpdateBy(SecurityUtils.getUsername());
        bill.setUpdateTime(new Date());

        if (!this.updateById(bill)) {
            throw new ServiceException("系统错误，票据提交失败！");
        }
    }

    // ==================== 删除 ====================

    @Override
    public void deleteBill(Long id) {
        checkNotAdmin();
        BizBill bill = this.getById(id);
        if (bill == null) {
            throw new ServiceException("票据不存在！");
        }

        // 仅草稿可删除
        if (!"0".equals(bill.getStatus())) {
            throw new ServiceException("只能删除草稿状态的票据！");
        }

        // 校验数据归属
        if (!SecurityUtils.getUsername().equals(bill.getCreateBy())) {
            throw new ServiceException("只能删除自己的票据！");
        }

        // 先删附件，再删票据
        fileMapper.delete(new LambdaQueryWrapper<BizBillFile>()
            .eq(BizBillFile::getBillId, id));
        this.removeById(id);
    }

    // ==================== 审核 ====================

    @Override
    public void reviewBill(BizBillReviewDto dto) {
        // 校验 action 值合法性
        if (!List.of("1", "2").contains(dto.getAction())) {
            throw new ServiceException("无效的审核结果，action 必须为 1（通过）或 2（退回）！");
        }

        BizBill bill = this.getById(dto.getBillId());
        if (bill == null) {
            throw new ServiceException("票据不存在！");
        }

        // 仅已提交状态可审核
        if (!"1".equals(bill.getStatus())) {
            throw new ServiceException("只能审核已提交状态的票据！");
        }

        // 更新票据审核信息
        bill.setStatus(dto.getAction().equals("1") ? "2" : "3");
        bill.setAuditBy(SecurityUtils.getUsername());
        bill.setAuditTime(new Date());
        bill.setAuditComment(dto.getComment());
        bill.setUpdateBy(SecurityUtils.getUsername());
        bill.setUpdateTime(new Date());

        if (!this.updateById(bill)) {
            throw new ServiceException("系统错误，审核失败！");
        }

        // 记录审核历史
        BizAuditLog log = BizAuditLog.builder()
            .billId(dto.getBillId())
            .action(dto.getAction())
            .comment(dto.getComment())
            .auditBy(SecurityUtils.getUsername())
            .auditTime(new Date())
            .build();
        auditLogMapper.insert(log);
    }

    // ==================== 附件 ====================

    /**
     * 解析附件路径字符串，创建附件记录
     * @param billId 票据ID
     * @param attachment 逗号分隔的附件路径
     */
    private void saveAttachments(Long billId, String attachment) {
        if (StringUtils.isBlank(attachment)) {
            return;
        }

        String[] paths = attachment.split(",");
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i].trim();
            if (StringUtils.isBlank(path)) {
                continue;
            }

            BizBillFile file = BizBillFile.builder()
                .billId(billId)
                .fileName(path.substring(path.lastIndexOf("/") + 1))
                .filePath(path)
                .sortOrder(i)
                .createBy(SecurityUtils.getUsername())
                .createTime(new Date())
                .build();
            fileMapper.insert(file);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 批量查询类别ID对应的类别名称
     */
    private Map<Long, String> buildCategoryNameMap(List<BizBill> bills) {
        List<Long> categoryIds = bills.stream()
            .map(BizBill::getCategoryId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());

        if (categoryIds.isEmpty()) {
            return new java.util.HashMap<>();
        }

        List<BizCategory> categories = categoryMapper.selectBatchIds(categoryIds);
        return categories.stream()
            .collect(Collectors.toMap(BizCategory::getCategoryId, BizCategory::getCategoryName, (a, b) -> a, java.util.HashMap::new));
    }

    /**
     * 生成票据编号: BILL-yyyyMMdd-NNNN
     * 查询当日最大编号，序号递增
     */
    private String generateBillNo() {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "BILL-" + dateStr + "-";

        // 查询当日最后一条编号
        BizBill latest = this.getOne(new LambdaQueryWrapper<BizBill>()
            .likeRight(BizBill::getBillNo, prefix)
            .orderByDesc(BizBill::getBillNo)
            .last("LIMIT 1"), false);

        int seq = 1;
        if (latest != null && StringUtils.isNotBlank(latest.getBillNo())) {
            String[] parts = latest.getBillNo().split("-");
            seq = Integer.parseInt(parts[2]) + 1;
        }

        return String.format("BILL-%s-%04d", dateStr, seq);
    }

}
