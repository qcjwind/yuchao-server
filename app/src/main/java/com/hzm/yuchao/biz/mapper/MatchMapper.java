package com.hzm.yuchao.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.controller.app.req.MatchListRequest;
import com.hzm.yuchao.biz.controller.app.resp.MatchListVO;
import com.hzm.yuchao.biz.model.MatchDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MatchMapper extends BaseMapper<MatchDO> {

    @Select("<script>" +
            "SELECT m.*, v.venue_address from t_match m left join t_venue v " +
            "on m.venue_id = v.id " +
            "where m.status = 'ENABLE' " +
            "   <if test='request.matchName != null'>" +
            "       AND m.name LIKE CONCAT('%', #{request.matchName}, '%') " +
            "   </if>" +
            "   <if test='request.cityCode != null'>" +
            "       AND v.city_code = #{request.cityCode}" +
            "   </if>" +
            "   <if test='request.areaCode != null'>" +
            "       AND v.area_code = #{request.areaCode}" +
            "   </if>" +
            "   <if test='!request.grayUser'>" +
            "       AND m.gray_match = 'N'" +
            "   </if>" +
            "ORDER BY m.sale_status desc, start_time asc" +
            "</script>")
    IPage<MatchListVO> selectByPage(Page<MatchDO> page, MatchListRequest request);


}
