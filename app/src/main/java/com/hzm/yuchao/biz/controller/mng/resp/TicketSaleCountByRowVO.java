package com.hzm.yuchao.biz.controller.mng.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSaleCountByRowVO {

    @ApiModelProperty("大区")
    private String area;

    @ApiModelProperty("子区域")
    private String subArea;

    @ApiModelProperty("排")
    private int seatRow;

    @ApiModelProperty("未售数量")
    private int unsaleNum;

    @ApiModelProperty("待支付数量")
    private int waitPayNum;

    @ApiModelProperty("已售数量")
    private int saleNum;

}
