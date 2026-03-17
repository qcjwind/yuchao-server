package com.hzm.yuchao.biz.controller.app.req;

import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.simple.base.BaseAppRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class BuyTicketRequest extends BaseAppRequest {

    @NotNull
    @ApiModelProperty("购票列表")
    private Long skuId;

    @NotNull
    @ApiModelProperty("幂等，前端生成UUID")
    private String requestNo;

//    @Valid
//    @NotEmpty
    @ApiModelProperty("购票列表-json")
    private String listJsonStr;

//    @Valid
//    @NotEmpty
//    @Size(max = 8)
    @ApiModelProperty("购票列表，与listJsonStr 2选一，优先取值listJsonStr")
    private List<BuyTicket> list;

    @Data
    public static class BuyTicket {

        @ApiModelProperty("真实姓名，购票需要身份证时必填")
        private String name;

        @ApiModelProperty("证件类型，购票需要身份证时必填")
        private IdTypeEnum idType;

        @ApiModelProperty("证件号，购票需要身份证时必填")
        private String idNo;

        @NotNull
        @ApiModelProperty("电话号码，必填")
        private String mobile;

        @ApiModelProperty("是否购票人自己")
        private boolean mySelf;
    }
}
