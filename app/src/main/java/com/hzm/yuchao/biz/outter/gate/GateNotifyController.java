package com.hzm.yuchao.biz.outter.gate;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.OrderService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.simple.base.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Binary Wang
 * https://pay.weixin.qq.com/doc/v3/merchant/4012791911
 */
@Slf4j
@Api("微信支付")
@RestController
@RequestMapping("/gate")
public class GateNotifyController {

    @Resource
    private TicketService ticketService;

    @Resource
    private OrderService orderService;

    @Value("${gate.nameVerify.token}")
    private String token;

    public static void main(String[] args) {
        String data = "12132432aabbcc";
        String password = "9EC6A1B7C07A4576B48DFE62A0E69DF0";
        // 你用这个加密
        String encryptHex = SecureUtil.aes(password.getBytes()).encryptHex(data);

        // 我用这个解密
        String decryptStr = SecureUtil.aes(password.getBytes()).decryptStr(encryptHex);

        System.out.println(encryptHex);
        System.out.println(decryptStr);

        System.out.println(SecureUtil.aes(password.getBytes()).decrypt("3c603826df9730df71f34cbd3cc725db5b8bba465a1ae9efb7b5a34156a49d8f65509c377b4605fc58ca085657285c66"));
    }

    @ApiOperation(value = "核销")
    @RequestMapping("/verification")
    public BaseResponse verification(String thirdId) {

        try {
            String bid = SecureUtil.aes(token.getBytes()).decryptStr(thirdId);

            LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(TicketDO::getVerificationStatus, BooleanEnum.Y);
            updateWrapper.eq(TicketDO::getBid, bid);

            boolean update = ticketService.update(updateWrapper);

            if (update) {
                return BaseResponse.success();
            } else {
                return BaseResponse.failure(500, "thirdId不存在");
            }

        } catch (Exception e) {
            log.error("更新核销状态失败，{}", thirdId, e);
            return BaseResponse.failure(500, e.getMessage());
        }
    }
}

