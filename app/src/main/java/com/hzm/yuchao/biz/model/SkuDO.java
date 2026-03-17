package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hzm.yuchao.biz.enums.SkuStatusEnum;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * sku
 */
@Data
@TableName("t_sku")
public class SkuDO extends BaseDO {

    @ApiModelProperty("赛程id")
    private Long matchId;

    @ApiModelProperty("场馆id")
    private Long venueId;

    @ApiModelProperty("sku名称")
    private String skuName;

    @ApiModelProperty("sku类型，售票和赠票")
    private TicketTypeEnum skuType;

    @ApiModelProperty("sku 状态")
    private SkuStatusEnum skuStatus;

    @ApiModelProperty("区域")
    private String area;

    @ApiModelProperty("价格，单位分")
    private Integer price;

    @ApiModelProperty("总票数")
    private Integer totalTicket;

    @ApiModelProperty("库存票数")
    private Integer stockTicket;

    @ApiModelProperty("排序值")
    private Integer sortNumber;

    @ApiModelProperty("备注，内部使用，不对外展示")
    private String remark;

    @ApiModelProperty("富文本描述")
    private String description;

}
