package com.hzm.yuchao.biz.dto;

import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatisticsDTO {

    @ApiModelProperty("状态")
    private OrderStatusEnum orderStatus;

    @ApiModelProperty(value = "购买数量")
    private Integer buyNum;

    @ApiModelProperty(value = "总价, 单位分")
    private int totalPrice;

    @ApiModelProperty(value = "次数")
    private int times;

}
