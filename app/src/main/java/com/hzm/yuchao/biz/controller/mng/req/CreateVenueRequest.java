package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CreateVenueRequest extends BaseRequest {

    @ApiModelProperty("id, 新增的时候不要传")
    private Long id;

    @NotEmpty
    private String name;

//    @NotEmpty
    private String detail;

    @NotEmpty
    @ApiModelProperty("场馆经度")
    private String venueLng;

    @NotEmpty
    @ApiModelProperty("场馆纬度")
    private String venueLat;

    @NotEmpty
    @ApiModelProperty("场馆地址")
    private String venueAddress;

    @ApiModelProperty("省")
    private String provinceName;

    @NotEmpty
    @ApiModelProperty("省code")
    private String provinceCode;

    @ApiModelProperty("市")
    private String cityName;

    @NotEmpty
    @ApiModelProperty("市code")
    private String cityCode;

    @ApiModelProperty("区")
    private String areaName;

    @NotEmpty
    @ApiModelProperty("区code")
    private String areaCode;

    /**
     * demo: 参考DO的值
     */
//    @NotEmpty
    @ApiModelProperty("售票 json配置，含sku和价格信息")
    private String saleSkuInfo;

    @ApiModelProperty("赠票 json配置，含sku和价格信息")
    private String giftSkuInfo;

}
