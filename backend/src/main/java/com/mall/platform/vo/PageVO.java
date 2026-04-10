package com.mall.platform.vo;

import java.util.List;

/**
 * 通用分页返回对象。
 *
 * @param <T> 列表数据类型
 */
public class PageVO<T> {
    private Long total;
    private Long pageNum;
    private Long pageSize;
    private List<T> list;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
