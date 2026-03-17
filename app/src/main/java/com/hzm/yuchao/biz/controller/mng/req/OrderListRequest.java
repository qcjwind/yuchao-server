package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
public class OrderListRequest extends BaseRequest {

    @NotNull
    @ApiModelProperty("赛程ID")
    private Long matchId;

    @ApiModelProperty("购买人姓名 模糊搜索")
    private String name;

    @ApiModelProperty("订单编号 模糊搜索")
    private String orderNo;

    @ApiModelProperty("订单状态")
    private OrderStatusEnum orderStatus;

    @ApiModelProperty("下单 开始时间")
    private Date beginTime;

    @ApiModelProperty("下单 结束时间")
    private Date endTime;

    private Integer pageSize = 10;

    private Integer pageNumber = 1;

}
