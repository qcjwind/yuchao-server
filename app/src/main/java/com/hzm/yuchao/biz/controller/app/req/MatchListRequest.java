package com.hzm.yuchao.biz.controller.app.req;

import com.hzm.yuchao.simple.base.BaseAppRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchListRequest extends BaseAppRequest {

    @ApiModelProperty("赛程名称 模糊搜索")
    private String matchName;

    @ApiModelProperty("城市编码")
    private String cityCode;

    @ApiModelProperty("区县编码")
    private String areaCode;

    private Integer pageSize = 10;

    private Integer pageNumber = 1;

    private boolean grayUser;

}
