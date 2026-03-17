package com.hzm.yuchao.biz.controller.mng.resp;

import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.VenueDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MatchTotalVO {

    @ApiModelProperty("赛事信息")
    private MatchDO match;

    @ApiModelProperty("场馆信息")
    private VenueDO venue;

    @ApiModelProperty("sku信息，根据matchId查询时会有值")
    private List<SkuDO> skuList;

    @ApiModelProperty("注册用户数")
    private int userCount;

    @ApiModelProperty("购票是否需要身份证")
    private BooleanEnum needIdForTicket;

    public MatchTotalVO(MatchDO match, VenueDO venue, List<SkuDO> skuList, int userCount) {
        this.match = match;
        this.venue = venue;
        this.skuList = skuList;
        this.userCount = userCount;
        this.needIdForTicket = match != null ? match.getNeedIdForTicket() : null;
    }
}
