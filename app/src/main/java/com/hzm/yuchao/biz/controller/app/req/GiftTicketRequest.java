package com.hzm.yuchao.biz.controller.app.req;

import com.hzm.yuchao.simple.base.BaseAppRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class GiftTicketRequest extends BaseAppRequest {

    @NotNull
    @ApiModelProperty("赠票bid")
    private String ticketBid;

}
