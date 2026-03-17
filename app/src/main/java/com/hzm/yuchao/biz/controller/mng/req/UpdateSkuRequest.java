package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateSkuRequest extends BaseRequest {

    @ApiModelProperty("id, 新增的时候不要传")
    private Long id;

    @NotBlank
    @ApiModelProperty("sku名称")
    private String skuName;

    @Min(0)
    @ApiModelProperty("价格，单位分，可以为0")
    private Integer price;

    @ApiModelProperty("排序值")
    private Integer sortNumber;

    @ApiModelProperty("备注，内部使用，不对外展示")
    private String remark;

    @ApiModelProperty("富文本描述")
    private String description;

}
