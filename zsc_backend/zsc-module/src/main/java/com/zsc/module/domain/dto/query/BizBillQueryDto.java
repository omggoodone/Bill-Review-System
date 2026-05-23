package com.zsc.module.domain.dto.query;

import com.zsc.module.common.pagination.BasePageReq;
import lombok.Data;

/**
 * 票据列表查询请求体
 * 普通用户自动过滤本人数据，管理员查全部
 */
@Data
public class BizBillQueryDto extends BasePageReq {

    /** 搜索关键词（标题/编号） */
    private String keywords;

    /** 按类别过滤 */
    private Long categoryId;

    /** 按状态过滤: 0-草稿 1-已提交 2-已通过 3-已退回 */
    private String status;

    /** 创建时间范围-开始 */
    private String startTime;

    /** 创建时间范围-结束 */
    private String endTime;

}
