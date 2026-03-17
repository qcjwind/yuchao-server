package com.hzm.yuchao.biz.controller.app.req;

import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.simple.base.BaseAppRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class IdNoValidRequest extends BaseAppRequest {

    @NotNull
    @ApiModelProperty("真实姓名")
    private String name;

    @NotNull
    @ApiModelProperty("id类型")
    private IdTypeEnum idType;

    @NotNull
    @ApiModelProperty("证件号")
    private String idNo;
}
