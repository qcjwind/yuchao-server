package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateSkuRequest extends BaseRequest {

    @ApiModelProperty("id, 新增的时候不要传")
    private Long id;

    @NotNull
    @ApiModelProperty("赛程id")
    private Long matchId;

    @NotBlank
    @ApiModelProperty("sku名称")
    private String skuName;

    @NotNull
    @ApiModelProperty("sku类型，售票和赠票")
    private TicketTypeEnum skuType;

    @Min(0)
    @ApiModelProperty("价格，单位分，可以为0")
    private Integer price;

    @Min(0)
    @ApiModelProperty("总票数")
    private Integer totalTicket;

    @Min(0)
    @ApiModelProperty("库存票数")
    private Integer stockTicket;

    @ApiModelProperty("排序值")
    private Integer sortNumber;

    @ApiModelProperty("备注，内部使用，不对外展示")
    private String remark;

    @ApiModelProperty("富文本描述")
    private String description;

}
