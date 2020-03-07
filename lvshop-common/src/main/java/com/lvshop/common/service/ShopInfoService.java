package com.lvshop.common.service;

import com.lvshop.common.pojo.ShopInfo;

/**
 * @author Galliano
 */
public interface ShopInfoService {
    /**
     * 往MySQL新增店铺信息
     * @param shopInfo
     */
    void saveShopInfo(ShopInfo shopInfo);

    /**
     * 根据店铺id在MySQL中删除店铺信息
     * @param shopId
     */
    void removeShopInfoById(Long shopId);

    /**
     * 在MySQL中更新店铺信息
     * @param shopInfo
     */
    void updateShopInfo(ShopInfo shopInfo);

    /**
     * 根据店铺id在MySQL中查询店铺信息
     */
    ShopInfo findShopInfoById(Long shopId);

}
