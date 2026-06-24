package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.BizCategoryDto;
import com.zsc.module.domain.dto.query.BizCategoryQueryDto;
import com.zsc.module.domain.entity.BizBill;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.mapper.BizBillMapper;
import com.zsc.module.mapper.BizCategoryMapper;
import com.zsc.module.service.BizCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 业务类别表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-15
 */
@Service
public class BizCategoryServiceImpl extends ServiceImpl<BizCategoryMapper, BizCategory> implements BizCategoryService {

    @Autowired
    private BizBillMapper billMapper;

    private static final String DEFAULT_CATEGORY = "其他";

    /**
     * 添加业务类别
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(BizCategoryDto addDto) {
        BizCategory category = new BizCategory();
        
        // 将DTO数据复制到实体类
        BeanUtils.copyProperties(addDto, category);
        
        // 设置默认字段
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        
        // 保存对象
        if (!this.save(category)) {
            throw new ServiceException("系统错误，业务类别添加失败！");
        }
    }

    /**
     * 更新业务类别
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(BizCategoryDto updateDto) {
        BizCategory existing = this.getById(updateDto.getCategoryId());
        if (existing == null) {
            throw new ServiceException("类别不存在");
        }
        if (DEFAULT_CATEGORY.equals(existing.getCategoryName())) {
            throw new ServiceException("「其他」是默认类别，不可修改");
        }
        BizCategory category = new BizCategory();
        BeanUtils.copyProperties(updateDto, category);
        category.setUpdateTime(new Date());
        if (!this.updateById(category)) {
            throw new ServiceException("系统错误，业务类别更新失败！");
        }
    }

    /**
     * 复杂条件查询
     */
    @Override
    public PageResult queryCategories(BizCategoryQueryDto queryDto) {
        Page<BizCategory> result = this.lambdaQuery()
                .like(StringUtils.isNotBlank(queryDto.getCategoryName()), 
                      BizCategory::getCategoryName, queryDto.getCategoryName())
                .eq(StringUtils.isNotBlank(queryDto.getStatus()), 
                    BizCategory::getStatus, queryDto.getStatus())
                .orderByAsc(BizCategory::getSortOrder)
                .orderByDesc(BizCategory::getCreateTime)
                .page(queryDto.convertToPage());

        return PageResult.fromPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        BizCategory category = this.getById(id);
        if (category == null) {
            throw new ServiceException("类别不存在");
        }
        if (DEFAULT_CATEGORY.equals(category.getCategoryName())) {
            throw new ServiceException("「其他」是默认类别，不可删除");
        }
        // 找到"其他"类别
        BizCategory defaultCat = this.lambdaQuery()
            .eq(BizCategory::getCategoryName, DEFAULT_CATEGORY)
            .one();
        if (defaultCat == null) {
            throw new ServiceException("系统错误：默认类别「其他」不存在");
        }
        // 将该类别下的票据迁移到"其他"
        billMapper.update(null,
            new LambdaUpdateWrapper<BizBill>()
                .eq(BizBill::getCategoryId, id)
                .set(BizBill::getCategoryId, defaultCat.getCategoryId()));
        // 删除类别
        if (!this.removeById(id)) {
            throw new ServiceException("删除失败");
        }
    }
}
