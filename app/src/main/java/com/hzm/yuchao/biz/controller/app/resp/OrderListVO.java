package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.model.BaseDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class OrderListVO extends BaseDO {

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

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("微信支付id, 仅需要支付的订单有")
    private String wxPrepayId;

    @ApiModelProperty("user id")
    private long userId;

    @ApiModelProperty("sku id")
    private long skuId;

    @ApiModelProperty("商品名称")
    private String skuName;

    @ApiModelProperty("购买数量")
    private int buyNum;

    @ApiModelProperty("总价，单位分")
    private int totalPrice;

    @ApiModelProperty("退款金额, 单位分")
    private Integer refundPrice;

    @ApiModelProperty("支付状态")
    private OrderStatusEnum orderStatus;

    @ApiModelProperty("赛程开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("赛程结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty("下单时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    @ApiModelProperty("支付时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @ApiModelProperty("退款时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundTime;

    @ApiModelProperty("支付相关参数")
    private String payInfo;

    @ApiModelProperty("支付相关参数")
    private String orderInfo;
}
