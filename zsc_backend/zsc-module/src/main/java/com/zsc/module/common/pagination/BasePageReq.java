package com.zsc.module.common.pagination;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Setter;

/**
 * description: 封装分页的参数,分页查询的dto需要继承该基类
 *
 * @author: hevean
 * @date: 2022/04/30
 */
@Setter
public class BasePageReq {
    /** 最大分页记录**/
    private final static int MAX_PAGESIZE = 100;

    /** 默认分页记录 **/
    private final static int DEFAULT_PAGESIZE = 10;

    @Max(value = 100, message = "每页大小不能超过100条记录")
    @Min(value = 1, message = "每页大小不能小于1")
    private int pageSize = DEFAULT_PAGESIZE;

    // 当前页码
    @Min(value = 1, message = "当前页不能小于1")
    private int currentPage = 1;

     // 如果当前页小于1，则取值为1
    public int getCurrent() {
        return this.currentPage <= 0 ? 1 : this.currentPage;
    }

    // 返回每页大小
    public int getPageSize() {
        return (this.pageSize <= 0 || this.pageSize > MAX_PAGESIZE) ? DEFAULT_PAGESIZE : this.pageSize;
    }

    // 获取分页对象
    public Page convertToPage() {
        return new Page(this.getCurrent(), this.getPageSize());
    }
}

