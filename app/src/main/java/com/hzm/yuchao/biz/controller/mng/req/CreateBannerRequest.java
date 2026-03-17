package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.simple.base.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NoArgsConstructor
public class CreateBannerRequest extends BaseRequest {

    @ApiModelProperty("主键，新增时不传")
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @NotNull
    @ApiModelProperty("图片地址")
    private String imageUrl;

    @ApiModelProperty("跳转类型：NONE/H5/MATCH/SKU/EXTERNAL")
    private String jumpType;

    @ApiModelProperty("跳转目标，如URL或matchId/skuId")
    private String jumpTarget;

    @ApiModelProperty("排序，数字越小越靠前")
    private Integer sortNumber;

    @ApiModelProperty("是否启用")
    private BooleanEnum status;

    @ApiModelProperty("开始生效时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("结束生效时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty("备注")
    private String remark;
}

