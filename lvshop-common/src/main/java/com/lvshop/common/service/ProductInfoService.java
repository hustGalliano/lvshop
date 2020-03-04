package com.lvshop.common.service;

import com.lvshop.common.pojo.ProductInfo;

/**
 * @author Galliano
 */
public interface ProductInfoService {
    /**
     * 往MySQL新增商品信息
     * @param productInfo
     */
    void saveProductInfo(ProductInfo productInfo);

    /**
     * 根据商品id在MySQL中删除商品信息
     * @param productId
     */
    void removeProductInfoById(Long productId);

    /**
     * 在MySQL中更新商品信息
     * @param productInfo
     */
    void updateProductInfo(ProductInfo productInfo);

    /**
     * 根据商品id在MySQL中查询商品信息
     */
    ProductInfo findProductInfoById(Long productId);

}
