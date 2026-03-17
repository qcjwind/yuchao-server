package com.hzm.yuchao.biz.clients;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class GateNameVerifyRequest {

    private String name;

    // 证件号
    private String idNumber;

}
