package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("t_banner")
public class BannerDO extends BaseDO {

    @ApiModelProperty("标题")
    private String title;

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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("结束生效时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否删除，逻辑删除标记")
    private BooleanEnum deleted;
}

