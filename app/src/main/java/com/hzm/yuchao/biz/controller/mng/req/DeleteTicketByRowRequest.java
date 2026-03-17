package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DeleteTicketByRowRequest extends BaseRequest {

    @NotNull
    @ApiModelProperty("赛程id")
    private Long matchId;

    @NotNull
    @ApiModelProperty("赛程id")
    private Long skuId;

    @NotEmpty
    @ApiModelProperty("大区")
    private String area;

    @NotEmpty
    @ApiModelProperty("子区域")
    private String subArea;

    @NotNull
    @ApiModelProperty("排数")
    private Integer seatRow;
}
