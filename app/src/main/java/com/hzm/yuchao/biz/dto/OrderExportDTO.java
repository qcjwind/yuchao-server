package com.hzm.yuchao.biz.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.utils.EnumDescConverter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@ColumnWidth(15)
public class OrderExportDTO {

    @ColumnWidth(10)
    @ExcelProperty("id")
    private Long id;

//    @ColumnWidth(25)
//    @ExcelProperty(value = "创建时间")
//    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
//    private Date gmtCreate;

    @ColumnWidth(40)
    @ExcelProperty(value = "订单编号")
    private String orderNo;

    @ExcelProperty(value = "下单人姓名")
    private String name;

    @ApiModelProperty("商品名称")
    private String skuName;

    @ExcelProperty(value = "购买数量")
    private Integer buyNum;

    @ExcelProperty(value = "总价, 单位元")
    private String totalPrice;

    @ExcelProperty(value = "退款金额, 单位元")
    private String refundPrice;

    @ExcelProperty(value = "支付状态", converter = EnumDescConverter.class)
    private OrderStatusEnum orderStatus;

    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "下单时间")
    private Date orderTime;

    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "支付时间")
    private Date payTime;

    @ColumnWidth(25)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty(value = "退款时间")
    private Date refundTime;

    @ExcelProperty(value = "支付相关备注")
    private String payInfo;

}
