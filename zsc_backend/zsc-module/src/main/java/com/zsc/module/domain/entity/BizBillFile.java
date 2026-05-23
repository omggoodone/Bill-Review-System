package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 票据附件表实体
 * 一张票据 (biz_bill) 可对应多个附件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_bill_file")
public class BizBillFile {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联票据ID */
    private Long billId;

    /** 原始文件名 */
    private String fileName;

    /** 服务器存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型 / 扩展名 */
    private String fileType;

    /** 附件排序（同票据内显示顺序） */
    private Integer sortOrder;

    /** 上传者用户名 */
    private String createBy;

    /** 上传时间 */
    private Date createTime;

}
