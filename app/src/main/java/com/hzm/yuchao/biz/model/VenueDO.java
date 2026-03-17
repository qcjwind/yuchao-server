package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_venue")
public class VenueDO extends BaseDO {

    private String name;

    private String detail;

    @ApiModelProperty("场馆经度")
    private String venueLng;

    @ApiModelProperty("场馆纬度")
    private String venueLat;

    @ApiModelProperty("场馆地址")
    private String venueAddress;

    @ApiModelProperty("省")
    private String provinceName;

    @ApiModelProperty("省code")
    private String provinceCode;

    @ApiModelProperty("市")
    private String cityName;

    @ApiModelProperty("市code")
    private String cityCode;

    @ApiModelProperty("区")
    private String areaName;

    @ApiModelProperty("区code")
    private String areaCode;

    /**
     * demo:
     [{"skuName":"A区","area":"A区","price":0,"totalTicket":542},
     {"skuName":"B区","area":"B区","price":0,"totalTicket":541},
     {"skuName":"C区","area":"C区","price":0,"totalTicket":624},
     {"skuName":"E区","area":"E区","price":0,"totalTicket":1239},
     {"skuName":"F区","area":"F区","price":0,"totalTicket":1416},
     {"skuName":"G区","area":"G区","price":0,"totalTicket":1995}]
     */
    @ApiModelProperty("售票 json配置，含sku和价格信息")
    private String saleSkuInfo;

    /**
     * demo:
     [{"skuName":"A区","area":"A区","price":0,"totalTicket":567},
     {"skuName":"B区","area":"B区","price":0,"totalTicket":567},
     {"skuName":"C区","area":"C区","price":0,"totalTicket":386},
     {"skuName":"D区","area":"D区","price":0,"totalTicket":2538},
     {"skuName":"E区","area":"E区","price":0,"totalTicket":381},
     {"skuName":"F区","area":"F区","price":0,"totalTicket":156},
     {"skuName":"G区","area":"G区","price":0,"totalTicket":570}]
     */
    @ApiModelProperty("赠票 json配置，含sku和价格信息")
    private String giftSkuInfo;

}
