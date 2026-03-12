package com.gacha.resource;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 资源管理器测试类
 */
public class ResourceManagerTest {

    private ResourceManager resourceManager;

    @Before
    public void setUp() {
        resourceManager = new ResourceManager();
    }

    @Test
    public void testInitialState() {
        assertEquals("初始资源应该为0", 0, resourceManager.getAvailableResources());
        assertEquals("初始消耗应该为0", 0, resourceManager.getTotalConsumed());
        assertEquals("初始池子数应该为0", 0, resourceManager.getPoolCount());
    }

    @Test
    public void testFirstPoolResources() {
        resourceManager.enterNextPool();

        assertEquals("第一个池子应该有120抽", 120, resourceManager.getAvailableResources());
        assertEquals("池子计数应该为1", 1, resourceManager.getPoolCount());
    }

    @Test
    public void testSecondPoolResources() {
        resourceManager.enterNextPool(); // 第1池：120
        resourceManager.enterNextPool(); // 第2池：+80 = 200

        assertEquals("第二个池子应该有200抽", 200, resourceManager.getAvailableResources());
        assertEquals("池子计数应该为2", 2, resourceManager.getPoolCount());
    }

    @Test
    public void testMultiplePools() {
        resourceManager.enterNextPool(); // 第1期：120
        resourceManager.enterNextPool(); // 第2期：+80 = 200
        resourceManager.enterNextPool(); // 第3期：+80 = 280
        resourceManager.enterNextPool(); // 第4期：+60 = 340

        assertEquals("第四个池子应该有340抽", 340, resourceManager.getAvailableResources());
        assertEquals("池子计数应该为4", 4, resourceManager.getPoolCount());
    }

    @Test
    public void testLaterPoolResources() {
        resourceManager.enterNextPool(); // 第1期：120
        resourceManager.enterNextPool(); // 第2期：+80 = 200
        resourceManager.enterNextPool(); // 第3期：+80 = 280
        resourceManager.enterNextPool(); // 第4期：+60 = 340
        resourceManager.enterNextPool(); // 第5期：+60 = 400
        resourceManager.enterNextPool(); // 第6期：+60 = 460

        assertEquals("第六个池子应该有460抽", 460, resourceManager.getAvailableResources());
        assertEquals("池子计数应该为6", 6, resourceManager.getPoolCount());
    }

    @Test
    public void testConsumeResources() {
        resourceManager.enterNextPool(); // 120

        boolean success = resourceManager.tryConsume(50);

        assertTrue("消耗应该成功", success);
        assertEquals("剩余资源应该为70", 70, resourceManager.getAvailableResources());
        assertEquals("总消耗应该为50", 50, resourceManager.getTotalConsumed());
    }

    @Test
    public void testConsumeAllResources() {
        resourceManager.enterNextPool(); // 120

        boolean success = resourceManager.tryConsume(120);

        assertTrue("消耗应该成功", success);
        assertEquals("剩余资源应该为0", 0, resourceManager.getAvailableResources());
        assertEquals("总消耗应该为120", 120, resourceManager.getTotalConsumed());
    }

    @Test
    public void testConsumeMoreThanAvailable() {
        resourceManager.enterNextPool(); // 120

        boolean success = resourceManager.tryConsume(150);

        assertFalse("消耗应该失败", success);
        assertEquals("资源应该保持不变", 120, resourceManager.getAvailableResources());
        assertEquals("消耗应该为0", 0, resourceManager.getTotalConsumed());
    }

    @Test
    public void testHasEnoughResources() {
        resourceManager.enterNextPool(); // 120

        assertTrue("应该有足够资源（100）", resourceManager.hasEnoughResources(100));
        assertTrue("应该有足够资源（120）", resourceManager.hasEnoughResources(120));
        assertFalse("应该没有足够资源（150）", resourceManager.hasEnoughResources(150));
    }

    @Test
    public void testAccumulatedConsumption() {
        resourceManager.enterNextPool(); // 120

        resourceManager.tryConsume(30);  // 消耗30
        resourceManager.tryConsume(40);  // 消耗40
        resourceManager.tryConsume(20);  // 消耗20

        assertEquals("剩余资源应该为30", 30, resourceManager.getAvailableResources());
        assertEquals("总消耗应该为90", 90, resourceManager.getTotalConsumed());
    }

    @Test
    public void testConsumptionAcrossPools() {
        resourceManager.enterNextPool(); // 120
        resourceManager.tryConsume(100); // 剩余20

        resourceManager.enterNextPool(); // +80 = 100

        assertEquals("新池子资源应该累加", 100, resourceManager.getAvailableResources());
        assertEquals("总消耗应该保持100", 100, resourceManager.getTotalConsumed());
    }

    @Test
    public void testGetCurrentPoolNumber() {
        assertEquals("初始池子编号应该为0", 0, resourceManager.getCurrentPoolNumber());

        resourceManager.enterNextPool();
        assertEquals("第一个池子编号应该为1", 1, resourceManager.getCurrentPoolNumber());

        resourceManager.enterNextPool();
        assertEquals("第二个池子编号应该为2", 2, resourceManager.getCurrentPoolNumber());
    }

    @Test
    public void testGetTotalConsumed() {
        resourceManager.enterNextPool(); // 120

        assertEquals("初始消耗应该为0", 0, resourceManager.getTotalConsumed());

        resourceManager.tryConsume(50);
        assertEquals("消耗50后应该为50", 50, resourceManager.getTotalConsumed());

        resourceManager.tryConsume(30);
        assertEquals("再消耗30后应该为80", 80, resourceManager.getTotalConsumed());
    }
}
