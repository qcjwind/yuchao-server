package com.hzm.yuchao.biz.controller.mng.req;

import com.alibaba.fastjson.annotation.JSONField;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QrcodeImageRequest {

    @NotNull
    private Long matchId;

    @ApiModelProperty("skuId列表，多个 逗号分割，不要有空格")
    private String skuIds;

    @ApiModelProperty("本次生成的票码类型")
    private TicketTypeEnum ticketType;

    @ApiModelProperty("底图，为了节约oss流量，传base64")
    @JSONField(serialize = false)
    private String bgImage;

    @ApiModelProperty("底图，网络图片链接，如果此字段有值 会优先使用这个")
    private String bgImageUrl;

    private int qrcodeX;

    private int qrcodeY;

    private int qrcodeWidth;

    private int qrcodeHeight;

    private int textSize;

    @ApiModelProperty("字体颜色，如 #F8F8F8")
    private String fontColor;

    private int areaX;

    private int areaY;

    private int subAreaX;

    private int subAreaY;

    private int rowX;

    private int rowY;

    private int seatX;

    private int seatY;

}
