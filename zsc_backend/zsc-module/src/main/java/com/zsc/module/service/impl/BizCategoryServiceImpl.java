package com.zsc.module.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.BizCategoryDto;
import com.zsc.module.domain.dto.query.BizCategoryQueryDto;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.mapper.BizCategoryMapper;
import com.zsc.module.service.BizCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
@Transactional
public class BizCategoryServiceImpl extends ServiceImpl<BizCategoryMapper, BizCategory> implements BizCategoryService {

    /**
     * 添加业务类别
     */
    @Override
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
    public void updateCategory(BizCategoryDto updateDto) {
        BizCategory category = new BizCategory();
        
        // 将DTO转换为实体类
        BeanUtils.copyProperties(updateDto, category);
        
        // 设置更新时间
        category.setUpdateTime(new Date());
        
        // 进行增量更新
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
}
