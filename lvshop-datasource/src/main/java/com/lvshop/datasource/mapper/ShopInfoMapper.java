package com.lvshop.datasource.mapper;

import com.lvshop.common.pojo.ShopInfo;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 店铺信息Mapper
 * @author Galliano
 */
@Repository
public interface ShopInfoMapper {

    /**
     * 新增店铺信息
     * @param shopInfo
     */

    void saveShopInfo(ShopInfo shopInfo);

    /**
     * 根据店铺id删除店铺信息
     * @param shopId
     */
    void deleteShopInfoById(Long shopId);

    /**
     * 更新店铺信息
     * @param shopInfo
     */
    void updateShopInfo(ShopInfo shopInfo);

    /**
     * 根据店铺id查询店铺信息
     */
    ShopInfo findShopInfoById(Long shopId);


}
