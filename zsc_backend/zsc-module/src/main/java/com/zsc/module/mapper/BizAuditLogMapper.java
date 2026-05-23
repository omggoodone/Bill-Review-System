package com.zsc.module.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zsc.module.domain.entity.BizAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录表 Mapper
 */
@Mapper
public interface BizAuditLogMapper extends BaseMapper<BizAuditLog> {
}
