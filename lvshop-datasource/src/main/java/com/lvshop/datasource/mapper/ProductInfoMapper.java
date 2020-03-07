package com.lvshop.datasource.mapper;

import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.datasource.kafka.KafkaProducer;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;

/**
 * 商品信息Mapper
 * @author Galliano
 */

@Repository
public interface ProductInfoMapper {
    /**
     * 新增商品信息
     * @param productInfo
     */
//    @Options(useGeneratedKeys = )
    void saveProductInfo(ProductInfo productInfo);

    /**
     * 根据商品id删除商品信息
     * @param productId
     */
    void removeProductInfoById(Long productId);

    /**
     * 更新商品信息
     * @param productInfo
     */
    void updateProductInfo(ProductInfo productInfo);

    /**
     * 根据商品id查询商品信息
     */
    ProductInfo findProductInfoById(Long productId);
}
