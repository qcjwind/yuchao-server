package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.model.VenueDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfoVO {

    @ApiModelProperty("赛事信息")
    private MatchDO match;

    @ApiModelProperty("场馆信息")
    private VenueDO venue;

    @ApiModelProperty("sku信息，根据matchId查询时会有值")
    private List<SkuDO> skuList;

    @ApiModelProperty("票据信息，根据ticketBid查询时会有值")
    private TicketDO ticket;

}
