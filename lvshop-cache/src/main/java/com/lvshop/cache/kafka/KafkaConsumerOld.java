/*

package com.lvshop.cache.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;


*/
/**
 * kafka消费者线程
 * @author Galliano
 *//*


public class KafkaConsumer implements Runnable {

	private ConsumerConnector consumerConnector;
	private String topic;

	public KafkaConsumer(String topic) {
		this.consumerConnector = Consumer.createJavaConsumerConnector(
				createConsumerConfig());
		this.topic = topic;
	}

	@SuppressWarnings("rawtypes")
	public void run() {
		Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =
				consumerConnector.createMessageStreams(topicCountMap);

        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream)).start();
        }
	}


*/
/**
	 * kafka消费者的配置
	 * @return
	 *//*


	private static ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "hang1:2181, hang2:2181, hang3:2181");
        props.put("group.id", "lvshop-cache-group");
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }
}

*/
