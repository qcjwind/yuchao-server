package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
public class CreateMatchRequest extends BaseRequest {

    @ApiModelProperty("id, 新增的时候不要传")
    private Long id;

    @NotBlank
    @ApiModelProperty("名字")
    private String name;

    @NotBlank
    @ApiModelProperty("描述")
    private String detail;

    @NotBlank
    @ApiModelProperty("封面图")
    private String cover;

    @ApiModelProperty("场次名称")
    private String matineeName;

    @ApiModelProperty("场馆id")
    private Long venueId;

    @ApiModelProperty("闸机url")
    private String gateUrl;

    @ApiModelProperty("闸机token")
    private String gateToken;

    @ApiModelProperty("单人购买限制，默认2，前端可不传")
    private Integer buyLimit;

    @ApiModelProperty("购票协议 json")
    private String agreementInfo;

    @ApiModelProperty("票码展示相关 json")
    private String ticketShowInfo;

    @ApiModelProperty("是否允许退款")
    private BooleanEnum allowRefund;

    @ApiModelProperty("购票是否需要身份证")
    private BooleanEnum needIdForTicket;

    @ApiModelProperty("退票规则 json")
    private String refundRule;

    @ApiModelProperty("开始售票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startSaleTime;

    @ApiModelProperty("结束售票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endSaleTime;

    @ApiModelProperty("赛程开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("赛程结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
