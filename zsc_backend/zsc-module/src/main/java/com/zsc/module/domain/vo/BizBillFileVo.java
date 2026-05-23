package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 附件信息出参，嵌套在 BizBillDetailVo 内
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizBillFileVo {

    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 服务器存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型 / 扩展名 */
    private String fileType;

    /** 上传时间 */
    private Date createTime;

}
