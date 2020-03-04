package com.lvshop.cache.hystrix.command.out;

import com.alibaba.fastjson.JSONObject;
import com.lvshop.common.pojo.ProductInfo;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;


/**
 * 用HBase做冷备份
 * HBase中的数据，都是过时的数据，但可以用来撑撑场面
 * @author Galliano
 */
public class HBaseColdDataCommand extends HystrixCommand<ProductInfo> {

    private Long productId;

    public HBaseColdDataCommand(Long productId) {
        super(HystrixCommandGroupKey.Factory.asKey("HBaseGroup"));
        this.productId = productId;
    }

    // 这是第 1 级降级
    @Override
    protected ProductInfo run() throws Exception {
        // 假装这个是查询hbase得来的结果
        String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
        return JSONObject.parseObject(productInfoJSON, ProductInfo.class);
    }

    // 这是第 2 级降级
    @Override
    protected ProductInfo getFallback() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(productId);
        // 从内存中找一些残缺的数据拼装进去
        return productInfo;
    }
}