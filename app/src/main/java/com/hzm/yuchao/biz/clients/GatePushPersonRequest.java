package com.hzm.yuchao.biz.clients;

import lombok.Data;

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
public class GatePushPersonRequest {

    // 第三方id（你们系统里这条数据的主键ID）
    private String thirdId;
    private String orderNo;

    private String name;
    // 证件类型：1-身份证；2-护照；3-港澳台通行证
    private String certificateType;
    // 证件号
    private String certificateNo;
    // 手机号
    private String mobile;
    private String price;
    private Boolean refund;

    private String bigArea;
    private String smallArea;
    private String seatNo;


}
