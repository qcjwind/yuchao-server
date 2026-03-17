import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.MyProjectApplication;
import com.hzm.yuchao.biz.component.QrcodeComponent;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.TicketService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest(classes = MyProjectApplication.class)
public class QrCodeTest {

    @Resource
    private TicketService ticketService;

    @Resource
    private QrcodeComponent qrcodeComponent;

    // 测试用户查询方法
//    @Test
    void testGenerate() throws Exception {

        // 查询出本次的赠票
        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, 1L);
        queryWrapper.eq(TicketDO::getTicketType, TicketTypeEnum.GIFT_TICKET);
        queryWrapper.last("limit 10000");
        List<TicketDO> giftTicketList = ticketService.list(queryWrapper);

        if (CollectionUtils.isEmpty(giftTicketList)) {
            return;
        }

        System.out.println(JSONUtil.toJsonStr(giftTicketList));

        // 1000张赠票生成二维码 并打包上传oss.
//        System.out.println("普通模式的二维码：" + qrcodeComponentNew.generateZip(giftTicketList, false));
//        System.out.println("优化模式的二维码：" + qrcodeComponentNew.generateZip(giftTicketList, true));

    }

}
