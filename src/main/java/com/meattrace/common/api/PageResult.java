package com.meattrace.common.api;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结构。
 * <p>
 * 不直接把 PageHelper 的 {@link PageInfo} 暴露给前端，避免前端与分页插件实现耦合。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private int pages;

    /** 当前页码，从 1 开始 */
    private int current;

    /** 每页条数 */
    private int size;

    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> empty(int current, int size) {
        return new PageResult<>(0L, 0, current, size, Collections.emptyList());
    }

    /**
     * 由 PageHelper 分页结果转换而来。
     * <p>用法：{@code PageResult.of(PageHelper.startPage(1, 10) 之后的 list)}
     */
    public static <T> PageResult<T> of(List<T> list) {
        PageInfo<T> info = new PageInfo<>(list);
        return new PageResult<>(
                info.getTotal(),
                info.getPages(),
                info.getPageNum(),
                info.getPageSize(),
                info.getList()
        );
    }
}
