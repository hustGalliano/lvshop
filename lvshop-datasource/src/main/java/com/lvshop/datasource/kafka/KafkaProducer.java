package com.lvshop.datasource.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * kafka生产者
 * @author Galliano
 * 将数据源服务的数据变更信息，发送到生产者中，然后缓存服务会拉取消息，
 * 缓存服务根据消息内容，来远程调用数据源服务的方法。
 *
 * 测试通过
 */

@Service
public class KafkaProducer {
    private static final String TOPIC = "data_change";

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;


    public String send(String msg){
        // msg有3个字段：serviceId、productId、shoptId
        // serviceId有2种取值：productInfoService、shopInfoService
        kafkaTemplate.send(TOPIC, msg);

        LOGGER.info("向kafka broker发送消息成功 [topic = " + TOPIC + ", msg = " + msg + "]");

        return "success";
    }
}
