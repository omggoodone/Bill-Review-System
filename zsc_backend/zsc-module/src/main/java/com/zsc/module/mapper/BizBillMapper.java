package com.zsc.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsc.module.domain.entity.BizBill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 票据表 Mapper
 */
@Mapper
public interface BizBillMapper extends BaseMapper<BizBill> {
}
