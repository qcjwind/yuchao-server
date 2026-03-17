package com.hzm.yuchao.biz.controller.app;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hzm.yuchao.biz.clients.GateClient;
import com.hzm.yuchao.biz.clients.GateNameVerifyRequest;
import com.hzm.yuchao.biz.config.AppConfig;
import com.hzm.yuchao.biz.controller.app.req.IdNoValidRequest;
import com.hzm.yuchao.biz.controller.app.resp.IdNoValidVO;
import com.hzm.yuchao.biz.model.UserDO;
import com.hzm.yuchao.biz.service.UserService;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "APP-用户信息")
@CrossOrigin
@RestController
@RequestMapping("/app/user/")
public class UserAppController {

    @Resource
    private UserService userService;

    @Resource
    private GateClient gateClient;

    @ApiOperation("设置头像昵称")
    @PostMapping("update")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<AppConfig> config(String nickname, String avatarUrl) {

        if (StringUtils.isEmpty(nickname) && StringUtils.isEmpty(avatarUrl)) {
            return SimpleResponse.fail("头像和昵称不能同时为空");
        }

        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(StringUtils.isNotEmpty(nickname), UserDO::getNickname, nickname);
        updateWrapper.set(StringUtils.isNotEmpty(avatarUrl), UserDO::getAvatarUrl, avatarUrl);
        updateWrapper.eq(UserDO::getId, SessionUtils.getUserId());

        userService.update(updateWrapper);

        return SimpleResponse.ok();
    }


    @ApiOperation("实名校验，下游系统qps只有10")
    @PostMapping("idNoValid")
    @RateLimit(qps = 30)
    public SimpleResponse<IdNoValidVO> idNoValid(@Valid IdNoValidRequest request) {
        boolean result = gateClient.nameVerify(new GateNameVerifyRequest(request.getName(), request.getIdNo()));

        if (!result) {
            return SimpleResponse.fail("实名认证失败");
        }

        return SimpleResponse.ok(new IdNoValidVO(true));
    }
}
