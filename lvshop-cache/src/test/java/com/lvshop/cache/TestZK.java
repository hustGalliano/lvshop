package com.lvshop.cache;

import com.lvshop.cache.zk.ZooKeeperSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Galliano
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LvshopCacheApplication.class)
public class TestZK {

    ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

    @Test
    public void testSetNode() {
        zkSession.createNode("/lvhang");
        zkSession.setNodeData("/lvhang", "123");

        System.out.println(zkSession.getNodeData("/lvhang"));
    }
}
