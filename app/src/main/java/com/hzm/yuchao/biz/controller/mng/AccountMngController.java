package com.hzm.yuchao.biz.controller.mng;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hzm.yuchao.biz.controller.mng.req.LoginRequest;
import com.hzm.yuchao.biz.controller.mng.req.ModifyPasswordRequest;
import com.hzm.yuchao.biz.controller.mng.resp.LoginVO;
import com.hzm.yuchao.biz.model.AccountDO;
import com.hzm.yuchao.biz.service.AccountService;
import com.hzm.yuchao.simple.TokenDTO;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

@Api(tags = "MNG-系统用户")
@CrossOrigin
@RestController
@RequestMapping("/mng/account/")
public class AccountMngController {

    @Resource
    private AccountService accountService;

    @ApiOperation("登录")
    @PostMapping("login")
    public SimpleResponse<LoginVO> login(@Valid LoginRequest request) {

        LambdaQueryWrapper<AccountDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccountDO::getUsername, request.getUsername());

        AccountDO accountDO = accountService.getOne(queryWrapper);

        if (accountDO == null) {
            return SimpleResponse.fail("账户不存在");
        } else {
            String passwordAes = SecureUtil.md5(request.getPassword());

            if (!Objects.equals(accountDO.getPassword(), passwordAes)) {
                return SimpleResponse.fail("密码错误");
            }
        }

        TokenDTO tokenDTO = new TokenDTO(accountDO.getId(), accountDO.getUsername(), TokenDTO.PlatformEnum.MNG, System.currentTimeMillis());

        String token = SecureUtil.aes(Constants.AES_PASSWORD).encryptBase64(JSONObject.toJSONBytes(tokenDTO));

        LoginVO loginVO = new LoginVO(token);

        return SimpleResponse.ok(loginVO);
    }

    @ApiOperation("修改密码")
    @PostMapping("modifyPassword")
    public SimpleResponse<Object> modifyPassword(@Valid ModifyPasswordRequest request) {

        String passwordAes = SecureUtil.md5(request.getPassword());

        LambdaUpdateWrapper<AccountDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AccountDO::getPassword, passwordAes);
        updateWrapper.eq(AccountDO::getUsername, SessionUtils.getUser());

        accountService.update(updateWrapper);

        return SimpleResponse.ok();
    }

    public static void main(String[] args) {
        System.out.println(SecureUtil.md5("yc123."));
    }
}
