package com.hzm.yuchao.biz.dto;

import lombok.Data;

// 票码显示配置
@Data
public class TicketShowInfoDTO {

    private boolean area = true;
    private boolean subArea = true;
    private boolean seatRow = true;
    private boolean seatNo = true;

}
