package com.hzm.yuchao.biz.controller.mng.resp;

import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSeatVO {

    @ApiModelProperty("id")
    private long id;

    @ApiModelProperty("业务主键，用于生成二维码")
    private String bid;

    @ApiModelProperty("购买人 用户id，可能为空")
    private Long buyerId;

    @ApiModelProperty("区域")
    private String area;

    @ApiModelProperty("子区域")
    private String subArea;

    @ApiModelProperty("排")
    private int seatRow;

    @ApiModelProperty("号")
    private int seatNo;

    @ApiModelProperty("价格，单位分")
    private int price;

    @ApiModelProperty("销售状态")
    private TicketSaleStatusEnum saleStatus;

}
