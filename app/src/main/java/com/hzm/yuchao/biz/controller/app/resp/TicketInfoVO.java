package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.OrderDO;
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
public class TicketInfoVO {

    @ApiModelProperty("票据信息")
    private TicketDO ticket;

    @ApiModelProperty("赛事信息")
    private MatchDO match;

    @ApiModelProperty("场馆信息")
    private VenueDO arena;

}
