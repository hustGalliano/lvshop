package com.lvshop.datasource.controller;

import com.lvshop.datasource.kafka.KafkaProducer;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.service.ProductInfoService;
import com.lvshop.datasource.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品信息Controller
 * @author Galliano
 */
@RestController
public class ProductInfoController {
// http://localhost:8081/saveProductInfo?productId=2&name=xiaomi&price=2699&pictureList=mi1.jpg,mi2.jpg,mi3.jpg&specification=小米的规格&service=小米的售后服务&color=red,yellow,black&size=6.3&shopId=2
// String request = "localhost:8081/kafka/send/serviceId=" + productServiceId + "&productId=" + productInfo.getId();

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInfoController.class);
    private static final String productServiceId = "productInfoService";

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private KafkaProducer kafkaProducer;


    @RequestMapping("/saveProductInfo")
    public Response saveProductInfo(ProductInfo productInfo) {
        LOGGER.info("[ 接收到新增商品信息的请求 ] 商品id = {}, 名称 = {}, 价格 = {}, 图片列表 = {}, 说明 = {}," +
                " 服务 = {}, 颜色 = {}, 尺寸 = {}, 店铺id = {}", productInfo.getId(), productInfo.getName(),
                productInfo.getPrice(), productInfo.getPictureList(), productInfo.getSpecification(),
                productInfo.getService(), productInfo.getColor(), productInfo.getSize(), productInfo.getShopId());

        try {
            productInfoService.saveProductInfo(productInfo);

            LOGGER.info("[ 新增商品时间 = {} ]", productInfo.getModifiedTime());

            return new Response(Response.SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(Response.FAILURE);
        }
    }

    @RequestMapping("/removeProductInfo")
    public Response removeProductInfo(Long productId) {
        LOGGER.info("[ 接收到删除商品信息的请求 ] 商品id = {}", productId);

        try {
            productInfoService.removeProductInfoById(productId);

//            // 将productInfo的变更消息写到KafkaProducer中
//            String request = "localhost:8081/kafka/send/serviceId=" + productServiceId + "&productId=" + productId;

            return new Response(Response.SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(Response.FAILURE);
        }
    }

    @RequestMapping("/updateProductInfo")
    public Response updateProductInfo(ProductInfo productInfo) {
        LOGGER.info("[ 接收到更新商品信息的请求 ] id = {}, name = {}, price = {}, pictureList = {}, specification = {}, " +
                "service = {}, color = {}, size = {}, shopId = {}", productInfo.getId(), productInfo.getName(),
                productInfo.getPrice(), productInfo.getPictureList(), productInfo.getSpecification(),
                productInfo.getService(),  productInfo.getColor(), productInfo.getSize(), productInfo.getShopId());

        try {
            productInfoService.updateProductInfo(productInfo);

            LOGGER.info("[ 更新商品时间 = {} ]", productInfo.getModifiedTime());

            // operation:update || delete
            // 将productInfo的变更消息写到KafkaProducer中
            String msg = "{\"serviceId\":\"" + productServiceId + "\",\"productId\":" + productInfo.getId() + "}";
            kafkaProducer.send(msg);

            return new Response(Response.SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(Response.FAILURE);
        }
    }


    @RequestMapping("/getProductInfo")
    public ProductInfo getProductInfo(Long productId) {
        LOGGER.info("[ 接收到查询商品信息的请求 ] 商品id = {}", productId);
        ProductInfo productInfo = null;

        try {
            productInfo = productInfoService.findProductInfoById(productId);

            return productInfo;
        } catch (Exception e) {
            return null;
        }
    }
}
