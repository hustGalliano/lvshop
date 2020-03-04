package com.lvshop.datasource.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Kafka分区器
 * @author Galliano
 */
public class KafkaPartitioner implements Partitioner {
    /**
     * 获取要分区到的分区编号
     * @param topic
     * @param key
     * @param keyBytes
     * @param value
     * @param valueBytes
     * @param cluster
     * @return
     */
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 根据topic和cluster中broker的数量获得PartitionInfo列表
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);

        int numPartitions = partitions.size();

        // 若没有key，则直接根据分区数量进行分区
        if (key == null) {
            Random rand = new Random();
            // nextInt(n)将返回一个随机数，即：0 <= nextInt(n) < n。
            return rand.nextInt(numPartitions);
        }

        // 若有key，则根据key的hash值和分区数量进行分区
        int floorMod = Math.floorMod(key.hashCode(), numPartitions);
        return floorMod;
    }



    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
