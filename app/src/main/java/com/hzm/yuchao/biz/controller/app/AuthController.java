package com.hzm.yuchao.biz.controller.app;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.biz.clients.GateClient;
import com.hzm.yuchao.biz.clients.GateNameVerifyRequest;
import com.hzm.yuchao.biz.clients.WechatClient;
import com.hzm.yuchao.biz.controller.app.req.RegisterRequest;
import com.hzm.yuchao.biz.controller.app.resp.LoginVO;
import com.hzm.yuchao.biz.dto.WechatSession;
import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.biz.model.UserDO;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.biz.service.UserService;
import com.hzm.yuchao.biz.utils.WechatPhoneDecryptUtil;
import com.hzm.yuchao.simple.TokenDTO;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import com.hzm.yuchao.simple.utils.DataMaskingUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@Api(tags = "APP-登录")
@CrossOrigin
@RestController
@RequestMapping("/app/auth/")
public class AuthController {

    @Resource
    private WechatClient wechatClient;

    @Resource
    private UserService userService;

    @Resource
    private TicketService ticketService;

    @Resource
    private GateClient gateClient;

    @ApiOperation("第一步-静默授权")
    @PostMapping("login")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<LoginVO> login(String code) {

        // 1. 验证code是否存在
        if (code == null || code.isEmpty()) {
            return SimpleResponse.fail("缺少code参数");
        }

        // 2. 调用微信接口获取openid和session_key
        WechatSession session = wechatClient.getSessionInfo(code);

        // 3. 处理微信返回的错误
        if (session.getErrcode() != null && session.getErrcode() != 0) {
            return SimpleResponse.fail("微信接口调用失败: " + session.getErrmsg());
        }

        // 4. 检查用户是否存在，不存在则创建
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getOpenid, session.getOpenid());
        UserDO userDO = userService.getOne(queryWrapper);
        if (userDO == null) {
            return SimpleResponse.ok(new LoginVO(null, session.getSession_key(), session.getOpenid(), true, null));
        }

        TokenDTO tokenDTO = new TokenDTO(userDO.getId(), userDO.getName(), TokenDTO.PlatformEnum.APP, System.currentTimeMillis());
        String token = SecureUtil.aes(Constants.AES_PASSWORD).encryptBase64(JSONObject.toJSONBytes(tokenDTO));

        // 姓名脱敏
        userDO.setMaskName(DataMaskingUtil.maskName(userDO.getName()));
        userDO.setName(DataMaskingUtil.maskName(userDO.getName()));
        userDO.setIdNo(DataMaskingUtil.maskIdCard(userDO.getIdNo()));
        userDO.setMobile(DataMaskingUtil.maskPhone(userDO.getMobile()));

        return SimpleResponse.ok(new LoginVO(token, session.getSession_key(), session.getOpenid(), false, userDO));
    }


    @ApiOperation("第二步-注册")
    @PostMapping("register")
    @RateLimit(qps = 20)
    public SimpleResponse<LoginVO> register(@Valid RegisterRequest request) {

        // 1. 解密手机号
        String mobile = null;
        try {
            mobile = WechatPhoneDecryptUtil.decryptPhoneNumber(request.getEncryptedData(),
                    request.getSessionKey(), request.getIv());
        } catch (Exception e) {
            log.error("手机号解密异常", e);
        }

        if (!StringUtils.hasText(mobile)) {
            return SimpleResponse.fail("获取手机号失败");
        }

        // 2. 处理前端可能传 "" 的情况：归一化为空值
        String name = StringUtils.hasText(request.getName()) ? request.getName().trim() : null;
        IdTypeEnum idType = request.getIdType();
        String idNo = StringUtils.hasText(request.getIdNo()) ? request.getIdNo().trim() : null;

        // 3. 调用三方服务校验姓名和身份证（仅在必需字段齐全时校验）
        if (idType == IdTypeEnum.ID_CARD && StringUtils.hasText(name) && StringUtils.hasText(idNo)) {
            boolean result = gateClient.nameVerify(new GateNameVerifyRequest(name, idNo));

            if (!result) {
                return SimpleResponse.fail("实名认证失败");
            }
        }

        // 4. 存储用户信息
        UserDO userDO = new UserDO();
        userDO.setName(name);
        userDO.setIdType(idType);
        userDO.setIdNo(idNo);
        userDO.setOpenid(request.getOpenid());
        userDO.setMobile(mobile);
        try {
            userService.save(userDO);
        } catch (DuplicateKeyException e) {
            log.warn("注册唯一键异常, message: {}", e.getMessage());
            if (e.getMessage().contains("uk_openid")) {
                return SimpleResponse.fail("您已注册，请重新进入小程序");
            }
            return SimpleResponse.fail("证件号已被注册");
        }

        TokenDTO tokenDTO = new TokenDTO(userDO.getId(), userDO.getName(), TokenDTO.PlatformEnum.APP, System.currentTimeMillis());
        String token = SecureUtil.aes(Constants.AES_PASSWORD).encryptBase64(JSONObject.toJSONBytes(tokenDTO));

        // 姓名脱敏
        if (StringUtils.hasText(userDO.getName())) {
            userDO.setMaskName(DataMaskingUtil.maskName(userDO.getName()));
            userDO.setName(DataMaskingUtil.maskName(userDO.getName()));
        }
        if (StringUtils.hasText(userDO.getIdNo())) {
            userDO.setIdNo(DataMaskingUtil.maskIdCard(userDO.getIdNo()));
        }
        userDO.setMobile(DataMaskingUtil.maskPhone(userDO.getMobile()));

        return SimpleResponse.ok(new LoginVO(token, request.getSessionKey(), request.getOpenid(), false, userDO));
    }
}
