package com.hzm.yuchao.biz.controller.mng.resp;

import com.hzm.yuchao.biz.dto.SkuDTO;
import com.hzm.yuchao.biz.model.UserDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionAllVO {

    private UserDO userDO;

    private SkuDTO skuDTO;

}
