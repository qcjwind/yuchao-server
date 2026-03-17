package com.hzm.yuchao.biz.clients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 - 成功示例：
 {
 "code":0,
 "msg":"成功！",
 "data":null
 }
 - 失败示例：
 {
 "code":1,
 "msg":"第三方id不能为空！",
 "data":null
 }
 {
 "code":1,
 "msg":"身份证格式不正确！",
 "data":null
 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GateRefundPushRequest {

    // 第三方id（你们系统里这条数据的主键ID）
    private List<String> thirdIdList;

}
