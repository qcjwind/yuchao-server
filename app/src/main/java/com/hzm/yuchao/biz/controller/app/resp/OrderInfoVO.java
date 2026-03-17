package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.model.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoVO {

    @ApiModelProperty("订单信息")
    private OrderDO order;

    @ApiModelProperty("赛事信息")
    private MatchDO match;

    @ApiModelProperty("场馆信息")
    private VenueDO arena;

    @ApiModelProperty("票据信息")
    private List<TicketDO> ticketList;

}
