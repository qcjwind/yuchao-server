package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSyncStatusEnum;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.biz.model.TicketDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSeatListVO {

    public static List<TicketSeatListVO> build(List<TicketDO> ticketList) {
        List<TicketSeatListVO> result = new ArrayList<>();
        for (TicketDO ticketDO : ticketList) {
            TicketSeatListVO vo = new TicketSeatListVO();
            vo.setBid(ticketDO.getBid());
            vo.setTicket(ticketDO.getId());
            vo.setArea(ticketDO.getArea());
            vo.setSubArea(ticketDO.getSubArea());
            vo.setSeatRow(ticketDO.getSeatRow());
            vo.setSeatNo(ticketDO.getSeatNo());
            vo.setPrice(ticketDO.getPrice());
            vo.setTicketType(ticketDO.getTicketType());
            vo.setSaleStatus(ticketDO.getSaleStatus());
            vo.setSyncStatus(ticketDO.getSyncStatus());
            vo.setVerificationStatus(ticketDO.getVerificationStatus());
            result.add(vo);
        }
        return result;
    }

    @ApiModelProperty("订单提交座位信息")
    private Long ticket;

    @ApiModelProperty("业务主键，用于生成二维码")
    private String bid;

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

}
