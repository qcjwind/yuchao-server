package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hzm.yuchao.biz.enums.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("t_ticket")
@NoArgsConstructor
public class TicketDO extends BaseDO {

    public TicketDO(String area, String subArea, int seatRow, int seatNo) {
        this.area = area;
        this.subArea = subArea;
        this.seatRow = seatRow;
        this.seatNo = seatNo;
    }

    @ApiModelProperty("业务主键，用于生成二维码")
    private String bid;

    @ApiModelProperty("购票二维码内容，格式 YC-UUID")
    private String qrcode;

    @ApiModelProperty("赛事ID")
    private Long matchId;

    @ApiModelProperty("场馆ID")
    private Long venueId;

    // 赠票无sku id
    @ApiModelProperty("skuID")
    private Long skuId;

    // 赠票无订单id
    @ApiModelProperty("orderId")
    private Long orderId;

    @ApiModelProperty("购买人 用户id，可能为空")
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

    @ApiModelProperty("核销状态，默认N")
    private BooleanEnum verificationStatus;

    @ApiModelProperty("购票时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date saleTime;
}
