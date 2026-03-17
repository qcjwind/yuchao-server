package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class TicketListVO {

    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtModify;

    @ApiModelProperty("名字")
    private String matchName;

    @ApiModelProperty("封面图")
    private String matchCover;

    @ApiModelProperty("赛事ID")
    private long matchId;

    @ApiModelProperty("场馆ID")
    private long venueId;

    // 赠票无sku id
    @ApiModelProperty("skuID")
    private Long skuId;

    // 赠票无订单id
    @ApiModelProperty("orderId")
    private Long orderId;

    @ApiModelProperty("用户id，可能为空")
    private Long buyerId;

    @ApiModelProperty("身份类型")
    private IdTypeEnum idType;

    @ApiModelProperty("证件号")
    private String idNo;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("区域")
    private String area;

    @ApiModelProperty("子区域")
    private String subArea;

    @ApiModelProperty("排")
    private int seatRow;

    @ApiModelProperty("号")
    private int seatNo;

    @ApiModelProperty("价格，单位分")
    private int price;

    @ApiModelProperty("票类型")
    private TicketTypeEnum ticketType;

    @ApiModelProperty("销售状态")
    private TicketSaleStatusEnum saleStatus;

    @ApiModelProperty("同步闸机的状态")
    private TicketSyncStatusEnum syncStatus;

    @ApiModelProperty("核销状态")
    private BooleanEnum verificationStatus;

    @ApiModelProperty("购票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date saleTime;

    @ApiModelProperty("赛程开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("赛程结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

}
