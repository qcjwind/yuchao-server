package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.VenueDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyTicketVO {

    @ApiModelProperty("赛事信息")
    private MatchDO match;

    @ApiModelProperty("场馆信息")
    private VenueDO arena;

}
