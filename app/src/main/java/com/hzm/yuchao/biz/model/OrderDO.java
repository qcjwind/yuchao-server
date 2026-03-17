package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * order
 */
@Data
@TableName("t_order")
public class OrderDO extends BaseDO {

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("微信支付id, 仅需要支付的订单有")
    private String wxPrepayId;

    @ApiModelProperty("微信退款id, 仅退款的订单有")
    private String wxRefundId;

    @ApiModelProperty("user id")
    private Long userId;

    @ApiModelProperty("下单人真实姓名")
    private String name;

    @ApiModelProperty("赛程id")
    private Long matchId;

    @ApiModelProperty("场馆id")
    private Long venueId;

    @ApiModelProperty("sku id")
    private Long skuId;

    @ApiModelProperty("商品名称")
    private String skuName;

    @ApiModelProperty("购买数量")
    private Integer buyNum;

    @ApiModelProperty("总价, 单位分")
    private Integer totalPrice;

    @ApiModelProperty("退款金额, 单位分")
    private Integer refundPrice;

    @ApiModelProperty("支付状态")
    private OrderStatusEnum orderStatus;

    @ApiModelProperty("下单时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;

    @ApiModelProperty("支付成功时间，收到回调或者轮休的时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @ApiModelProperty("退款申请时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundTime;

    @ApiModelProperty("支付相关参数")
    private String payInfo;

    @ApiModelProperty("支付相关参数")
    private String orderInfo;

}
