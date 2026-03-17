package com.hzm.yuchao.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.controller.app.resp.TicketListVO;
import com.hzm.yuchao.biz.controller.mng.resp.TicketSaleCountByRowVO;
import com.hzm.yuchao.biz.dto.SubAreaStockDTO;
import com.hzm.yuchao.biz.model.TicketDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketMapper extends BaseMapper<TicketDO> {

    /**
     * 查询座位情况
     * @return
     */
    @Select("select sub_area, seat_row, IFNULL(count(*), 0) as stock from t_ticket " +
            "where sale_status = 'UNSOLD' AND ticket_type = 'SALE_TICKET' " +
            "AND sku_id = #{skuId} GROUP BY sub_area, seat_row having stock > #{ticketNum}")
    List<SubAreaStockDTO> selectUnsoldStock(@Param("skuId") long skuId, @Param("ticketNum") int ticketNum);


    @Select("select t.*, m.name as match_name, m.cover as match_cover, m.start_time, m.end_time " +
            " from t_ticket t left join t_match m " +
            " on t.match_id = m.id " +
            " where t.id_no = #{idNo} and t.id_type = #{idType} and t.sale_status = 'SOLD' " +
            " order by t.sale_time desc ")
    IPage<TicketListVO> selectMyTicketByPage(Page<TicketListVO> page, @Param("idNo") String idNo, @Param("idType") String idType);

    /**
     * 按排统计销售情况
     * @return
     */
    @Select("select area, sub_area, seat_row, " +
            "SUM(IF(sale_status = 'UNSOLD', 1, 0)) as unsale_num, " +
            "SUM(IF(sale_status = 'WAIT_PAY', 1, 0)) as wait_pay_num, " +
            "SUM(IF(sale_status = 'SOLD', 1, 0)) as sale_num " +
            "from t_ticket " +
            "where sku_id = #{skuId} " +
            "GROUP BY area, sub_area, seat_row ")
    List<TicketSaleCountByRowVO> saleCountByRow(@Param("skuId") long skuId);


}
