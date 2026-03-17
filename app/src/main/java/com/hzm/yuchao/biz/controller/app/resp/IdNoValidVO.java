package com.hzm.yuchao.biz.controller.app.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdNoValidVO {

    @ApiModelProperty("校验是否通过")
    private boolean valid;

}
