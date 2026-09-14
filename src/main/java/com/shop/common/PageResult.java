package com.shop.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果（配合 PageHelper 使用）
 */
@Data
public class PageResult<T> {
    private long total;
    private int pageNum;
    private int pageSize;
    /** 总页数 */
    private long pages;
    private List<T> list;

    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> list) {
        PageResult<T> p = new PageResult<>();
        p.total = total;
        p.pageNum = pageNum;
        p.pageSize = pageSize;
        p.pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        p.list = list == null ? Collections.emptyList() : list;
        return p;
    }
}
