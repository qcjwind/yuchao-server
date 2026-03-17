package com.hzm.yuchao.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzm.yuchao.biz.model.AccountDO;
import com.hzm.yuchao.biz.model.SkuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuMapper extends BaseMapper<SkuDO> {

    /**
     * 更新sku库存
     * @return
     */
    @Update("update t_sku set stock_ticket = (" +
            "SELECT count(*) from t_ticket where sku_id = #{skuId} and sale_status = 'UNSOLD'" +
            ") where id = #{skuId}")
    void updateSkuStock(@Param("skuId") long skuId);

    /**
     * 更新sku库存
     * @return
     */
    @Update("update t_sku set " +
            "stock_ticket = (SELECT count(*) from t_ticket where sku_id = #{skuId} and sale_status = 'UNSOLD'), " +
            "total_ticket = (SELECT count(*) from t_ticket where sku_id = #{skuId}) " +
            "where id = #{skuId}")
    void updateSkuTotalAndStock(@Param("skuId") long skuId);

}
