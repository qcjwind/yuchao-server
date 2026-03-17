package com.hzm.yuchao.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.controller.app.resp.OrderListVO;
import com.hzm.yuchao.biz.dto.OrderStatisticsDTO;
import com.hzm.yuchao.biz.model.OrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    @Select("SELECT IFNULL(SUM(buy_num), 0) FROM t_order WHERE " +
            "user_id = #{userId} and match_id = #{matchId} and order_status = 'PAY_SUCCESS'")
    int getTotalBuyNum(@Param("userId") long userId, @Param("matchId") long matchId);

    @Select("SELECT IFNULL(count(*), 0) FROM t_order WHERE " +
            "user_id = #{userId} and match_id = #{matchId} and order_status = 'WAIT_PAY'")
    int getWaitPayNum(@Param("userId") long userId, @Param("matchId") long matchId);

    @Select("select o.*, m.name as match_name, m.cover as match_cover, m.start_time, m.end_time " +
            " from t_order o left join t_match m " +
            " on o.match_id = m.id " +
            " where o.user_id = #{userId} " +
            " order by o.id desc ")
    IPage<OrderListVO> selectMyOrderByPage(Page<OrderListVO> page, @Param("userId") Long userId);


    @Select("SELECT order_status, sum(total_price) as total_price, sum(buy_num) as buy_num, count(*) as times FROM t_order " +
            "WHERE match_id = #{matchId} " +
            "group by order_status")
    List<OrderStatisticsDTO> statistics(@Param("matchId") long matchId);

}
