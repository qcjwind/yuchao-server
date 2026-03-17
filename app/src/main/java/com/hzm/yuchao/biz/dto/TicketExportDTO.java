package com.hzm.yuchao.biz.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSyncStatusEnum;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.biz.model.BaseDO;
import com.hzm.yuchao.biz.utils.EnumDescConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@ColumnWidth(15)
public class TicketExportDTO {

    @ColumnWidth(10)
    @ExcelProperty("id")
    private Long id;

    @ColumnWidth(25)
    @ExcelProperty(value = "创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date gmtCreate;

//    @ExcelProperty("修改时间")
//    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
//    private Date gmtModify;

//    @ExcelProperty("唯一标识")
//    private String bid;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty(value = "证件类型", converter = EnumDescConverter.class)
    private IdTypeEnum idType;

    @ColumnWidth(40)
    @ExcelProperty("证件号")
    private String idNo;

    @ExcelProperty("区域")
    private String area;

    @ExcelProperty("子区域")
    private String subArea;

    @ExcelProperty("排")
    private int seatRow;

    @ExcelProperty("号")
    private int seatNo;

    @ExcelProperty("价格，单位元")
    private String price;

    @ExcelProperty(value = "票类型", converter = EnumDescConverter.class)
    private TicketTypeEnum ticketType;

    @ExcelProperty(value = "销售状态", converter = EnumDescConverter.class)
    private TicketSaleStatusEnum saleStatus;

    @ExcelProperty(value = "同步闸机的状态", converter = EnumDescConverter.class)
    private TicketSyncStatusEnum syncStatus;

    @ColumnWidth(25)
    @ExcelProperty("购票时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private Date saleTime;
}
