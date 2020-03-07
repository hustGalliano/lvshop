package com.lvshop.datasource.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lvshop.datasource.mapper.ProductInfoMapper;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.service.ProductInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 商品信息Service实现类
 * @author Galliano
 */

@Service // Dubbo暴露服务
@Component("productInfoService")
public class ProductInfoServiceImpl implements ProductInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInfoServiceImpl.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ProductInfoMapper productInfoMapper;


    @Override
    public void saveProductInfo(ProductInfo productInfo) {
        String nowTime = sdf.format(new Date());

        productInfo.setModifiedTime(nowTime);

        productInfoMapper.saveProductInfo(productInfo);

    }

    @Override
    public void removeProductInfoById(Long productId) {
        productInfoMapper.removeProductInfoById(productId);
    }

    @Override
    @Transactional
    public void updateProductInfo(ProductInfo productInfo) {
        Long productId = productInfo.getId();
        ProductInfo productInfoFromMysql = findProductInfoById(productId);

        String nowTime = sdf.format(new Date());
        productInfo.setModifiedTime(nowTime);

        // 若商品存在，则更新，若不存在，则创建
        if (productInfoFromMysql != null) {
            productInfoMapper.updateProductInfo(productInfo);
        } else {
            LOGGER.info("[ 数据库中无此商品数据，更新商品请求 ==> 新增商品请求 ]");
            productInfoMapper.saveProductInfo(productInfo);
        }
    }

    @Override
    public ProductInfo findProductInfoById(Long productId) {
        return productInfoMapper.findProductInfoById(productId);
    }
}
