package com.hzm.yuchao.biz.component;

import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.SkuService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.biz.service.VenueService;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.tools.TicketSqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TicketGeneratorComponent {

    @Resource
    private TicketService ticketService;

    @Resource
    private MatchService matchService;

    @Resource
    private VenueService venueService;

    @Resource
    private SkuService skuService;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_TEMPLATE = "INSERT INTO t_ticket (bid, match_id, venue_id, sku_id, price, ticket_type, area, sub_area, seat_row, seat_no) VALUES ";

    private static final String VALUE_TEMPLATE = "('%s', ?, ?, ?, ?, '?', '%s', '%s', %d, %d)";

    // 将target转义为正则字面量（特殊字符被转义）
    private static final String ESCAPED_TARGET = Pattern.quote("?");

    /**
     * 全量生成sku 及 票码
     */
    @Transactional
    public void generateTicket(MatchDO matchDO, String[][] saleTicketAll, String[][] giftTicketAll) {

        if (saleTicketAll != null) {
            // 售票
            Map<String, SkuDO> saleSkuMap = generateSkuMap(matchDO, TicketTypeEnum.SALE_TICKET, saleTicketAll);
            generateSaleTicket(matchDO, saleSkuMap, null, saleTicketAll);
        }

        if (giftTicketAll != null) {
            // 赠票
            Map<String, SkuDO> giftSkuMap = generateSkuMap(matchDO, TicketTypeEnum.GIFT_TICKET, giftTicketAll);
            generateGiftTicket(matchDO, giftSkuMap, null, giftTicketAll);
        }

    }

    /**
     * 增量生成票码
     *
     * @param skuDO
     * @param all
     */
    @Transactional
    public void generateTicket(SkuDO skuDO, String[][] all) {

        MatchDO matchDO = matchService.getById(skuDO.getMatchId());

        List<String[][]> areaArrayList = TicketSqlGenerator.splitByArea(all);

        if (areaArrayList.size() > 1) {
            throw new BizException("追加票码仅支持一个区域，请删除无用数据");
        }

        if (skuDO.getSkuType() == TicketTypeEnum.SALE_TICKET) {
            generateSaleTicket(matchDO, null, skuDO, all);
        } else {
            generateGiftTicket(matchDO, null, skuDO, all);
        }
    }

    private Map<String, SkuDO> generateSkuMap(MatchDO matchDO, TicketTypeEnum typeEnum, String[][] all) {

        if (ArrayUtils.isEmpty(all)) {
            return new HashMap<>();
        }

        List<String[][]> areaArrayList = TicketSqlGenerator.splitByArea(all);

        List<SkuDO> saleSkuList = areaArrayList.stream().map(t -> {

            String skuName = t[0][0];
            int price = 0;

            SkuDO skuDO = new SkuDO();
            skuDO.setMatchId(matchDO.getId());
            skuDO.setVenueId(matchDO.getVenueId());
            skuDO.setSkuName(skuName);
            skuDO.setSkuType(typeEnum);
            skuDO.setArea(skuName);
            skuDO.setRemark(skuName);
            skuDO.setPrice(price);
            skuDO.setTotalTicket(0);
            skuDO.setStockTicket(0);
            return skuDO;
        }).collect(Collectors.toList());
        skuService.saveBatch(saleSkuList);

        // 转换为 Map<id, SkuDO>
        return saleSkuList.stream().collect(Collectors.toMap(SkuDO::getSkuName, Function.identity()));
    }

    private void generateSaleTicket(MatchDO matchDO, Map<String, SkuDO> skuMap, SkuDO sku, String[][] all) {

        List<String[][]> areaArrayList = TicketSqlGenerator.splitByArea(all);

        // 按每个大区生成8000张免费票
        for (String[][] areaArray : areaArrayList) {

            String area = areaArray[0][0];
            SkuDO skuDO = skuMap != null ? skuMap.get(area) : sku;

            if (skuDO == null) {
                throw new BizException(area + " 区域无SKU信息");
            }

            String tempTemplate = VALUE_TEMPLATE.replaceFirst(ESCAPED_TARGET, matchDO.getId() + "")
                    .replaceFirst(ESCAPED_TARGET, matchDO.getVenueId() + "")
                    .replaceFirst(ESCAPED_TARGET, skuDO.getId() + "")
                    .replaceFirst(ESCAPED_TARGET, skuDO.getPrice() + "")
                    .replaceFirst(ESCAPED_TARGET, TicketTypeEnum.SALE_TICKET.name());
            List<String> sqlList = TicketSqlGenerator.generateSingleArea(areaArray, SQL_TEMPLATE, tempTemplate);

            // 批量提交
            jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));

            skuMapper.updateSkuTotalAndStock(skuDO.getId());
        }

        log.info("售票生成完成，{}", matchDO.getId());
    }


    private void generateGiftTicket(MatchDO matchDO, Map<String, SkuDO> skuMap, SkuDO sku, String[][] all) {

        List<String[][]> areaArrayList = TicketSqlGenerator.splitByArea(all);

        // 按每个大区生成6000张免费票
        for (String[][] areaArray : areaArrayList) {

            String area = areaArray[0][0];
            SkuDO skuDO = skuMap != null ? skuMap.get(area) : sku;

            if (skuDO == null) {
                // 赠票sku 可以为空
                skuDO = new SkuDO();
                skuDO.setId(null);
                skuDO.setPrice(0);
            }

            String tempTemplate = VALUE_TEMPLATE.replaceFirst(ESCAPED_TARGET, matchDO.getId() + "")
                    .replaceFirst(ESCAPED_TARGET, matchDO.getVenueId() + "")
                    .replaceFirst(ESCAPED_TARGET, skuDO.getId() + "")
                    .replaceFirst(ESCAPED_TARGET, skuDO.getPrice() + "")
                    .replaceFirst(ESCAPED_TARGET, TicketTypeEnum.GIFT_TICKET.name());
            List<String> sqlList = TicketSqlGenerator.generateSingleArea(areaArray, SQL_TEMPLATE, tempTemplate);

            // 批量提交
            jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));

            skuMapper.updateSkuTotalAndStock(skuDO.getId());
        }

        log.info("赠票生成完成，{}", matchDO.getId());
//
//        // 查询出本次的赠票
//        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TicketDO::getMatchId, matchDO.getId());
//        queryWrapper.eq(TicketDO::getTicketType, TicketTypeEnum.GIFT_TICKET);
//        queryWrapper.last("limit 10000");
//        List<TicketDO> giftTicketList = ticketService.list(queryWrapper);
//
//        if (CollectionUtils.isEmpty(giftTicketList)) {
//            return;
//        }
//
//        // 1000张赠票生成二维码 并打包上传oss.
//        try {
//            String giftTicketUrl = qrcodeComponent.generateZip(giftTicketList);
//
//            LambdaUpdateWrapper<MatchDO> updateWrapper = new LambdaUpdateWrapper<>();
//            updateWrapper.set(MatchDO::getGiftTicketUrl, giftTicketUrl);
//            updateWrapper.eq(MatchDO::getId, matchDO.getId());
//            updateWrapper.isNull(MatchDO::getGiftTicketUrl);
//            matchService.update(updateWrapper);
//
//        } catch (Exception e) {
//            log.error("赠票文件生成异常, matchId: {}", matchDO.getId(), e);
//            throw new BizException("赠票文件生成异常");
//        }
//
//        log.info("赠票文件上传完成，matchId: {}", matchDO.getId());
    }

}
