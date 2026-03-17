package com.hzm.yuchao.simple.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> extends BaseResponse {

    private long total;

    private long pageNumber;

    private long pageSize;

    private List<T> data;

    public PageResponse(int code, String msg, List<T> data) {
        super(code, msg);
        this.data = data;
    }

    public static <T> PageResponse<T> ok() {
        return new PageResponse<T>(200, "success", null);
    }

    public static <T> PageResponse<T> ok(IPage<T> pageData) {
        PageResponse<T> pageResponse = PageResponse.ok();

        pageResponse.setData(pageData.getRecords());
        pageResponse.setPageSize(pageData.getSize());
        pageResponse.setPageNumber(pageData.getCurrent());
        pageResponse.setTotal(pageData.getTotal());

        return pageResponse;
    }

    public static <T> PageResponse<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> PageResponse<T> fail(int code, String msg) {
        return new PageResponse<T>(code, msg, null);
    }

}
