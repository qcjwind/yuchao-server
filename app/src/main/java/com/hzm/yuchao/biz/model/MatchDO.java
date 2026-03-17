package com.hzm.yuchao.biz.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.enums.MatchSaleStatusEnum;
import com.hzm.yuchao.biz.enums.MatchStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("t_match")
public class MatchDO extends BaseDO {

    @ApiModelProperty("名字")
    private String name;

    @ApiModelProperty("描述")
    @JSONField(serialize = false)
    private String detail;

    @ApiModelProperty("封面图")
    private String cover;

    @ApiModelProperty("场次名称")
    private String matineeName;

    // 状态. ENABLE 上架、 DISABLE 下架
    @ApiModelProperty("上下架状态")
    private MatchStatusEnum status;

    @ApiModelProperty("销售状态")
    private MatchSaleStatusEnum saleStatus;

    @ApiModelProperty("场馆id")
    private Long venueId;

    @ApiModelProperty("闸机url")
    private String gateUrl;

    @ApiModelProperty("闸机token")
    private String gateToken;

    @ApiModelProperty("赠票地址")
    private String giftTicketUrl;

    @ApiModelProperty("单人购买限制，默认2，前端可不传")
    private Integer buyLimit = 2;

    @ApiModelProperty("购票协议 json")
    @JSONField(serialize = false)
    private String agreementInfo;

    @ApiModelProperty("票码展示相关 json")
    @JSONField(serialize = false)
    private String ticketShowInfo;

    @ApiModelProperty("是否允许退款")
    private BooleanEnum allowRefund;

    @ApiModelProperty("购票是否需要身份证")
    private BooleanEnum needIdForTicket;

    @ApiModelProperty("退票规则 json")
    @JSONField(serialize = false)
    private String refundRule;

    @ApiModelProperty("开始售票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startSaleTime;

    @ApiModelProperty("结束售票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endSaleTime;

    @ApiModelProperty("赛程开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("赛程结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
