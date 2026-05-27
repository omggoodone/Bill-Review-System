package com.zsc.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.domain.dto.BizCategoryDto;
import com.zsc.module.domain.dto.query.BizCategoryQueryDto;
import com.zsc.module.domain.entity.BizCategory;

/**
 * <p>
 * 业务类别表 服务类
 * </p>
 *
 * @author author
 * @since 2026-04-15
 */
public interface BizCategoryService extends IService<BizCategory> {

    /**
     * 添加业务类别
     */
    void addCategory(BizCategoryDto addDto);

    /**
     * 更新业务类别
     */
    void updateCategory(BizCategoryDto updateDto);

    /**
     * 删除类别，将关联票据归类到"其他"
     */
    void deleteCategory(Long id);

    /**
     * 复杂条件查询，包含分页信息
     */
    PageResult queryCategories(BizCategoryQueryDto queryDto);
}
