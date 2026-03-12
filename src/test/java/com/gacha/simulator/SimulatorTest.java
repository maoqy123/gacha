package com.gacha.simulator;

import com.gacha.model.CardPool;
import com.gacha.statistics.StatisticsTracker;
import com.gacha.strategy.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 模拟器测试类 - 测试完整模拟流程
 */
public class SimulatorTest {

    private CardPool pool;

    @Before
    public void setUp() {
        pool = CardPool.createDefaultPool();
    }

    // ==================== 单池模拟测试 ====================

    @Test
    public void testSinglePoolSimulation_OneSixStarStrategy() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        GachaSimulator.SingleSimulationResult result = simulator.simulate(context, stats);

        assertNotNull("结果不应为空", result);
        assertTrue("抽卡次数应该大于0", result.getTotalDraws() > 0);
        assertTrue("应该获得卡牌", !result.getCards().isEmpty());
        assertTrue("6星数量应该大于等于1", result.getSixStarCount() >= 1);
    }

    @Test
    public void testSinglePoolSimulation_HardPityStrategy() {
        HardPityStrategy strategy = new HardPityStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        GachaSimulator.SingleSimulationResult result = simulator.simulate(context, stats);

        assertNotNull("结果不应为空", result);
        assertTrue("抽卡次数应该大于0", result.getTotalDraws() > 0);
        // 硬保底策略应该能拿到当期UP或者抽到120抽
        assertTrue("应该抽到120抽或者拿到当期UP",
                result.getPaidDraws() <= 120 || result.getCurrentUpCount() > 0);
    }

    @Test
    public void testSinglePoolSimulation_LoopStrategy() {
        LoopStrategy strategy = new LoopStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        GachaSimulator.SingleSimulationResult result = simulator.simulate(context, stats);

        assertNotNull("结果不应为空", result);
        assertTrue("抽卡次数应该大于0", result.getTotalDraws() > 0);
        // 第一个池子资源充足，应该目标120抽
        assertTrue("付费抽卡次数应该在合理范围内",
                result.getPaidDraws() > 0 && result.getPaidDraws() <= 120);
    }

    // ==================== 多池模拟测试 ====================

    @Test
    public void testMultiPoolSimulation_OneSixStarStrategy() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        // simulateMultiplePools会自动调用enterNextPool
        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(3, context, stats);

        assertNotNull("结果不应为空", result);
        assertEquals("应该有3个池子的结果", 3, result.getPoolResults().size());
        assertTrue("总消耗资源应该大于0", result.getTotalConsumedResources() > 0);
        assertTrue("6星总数应该大于等于3", result.getTotalSixStars() >= 3);
    }

    @Test
    public void testMultiPoolSimulation_HardPityStrategy() {
        HardPityStrategy strategy = new HardPityStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(3, context, stats);

        assertNotNull("结果不应为空", result);
        assertEquals("应该有3个池子的结果", 3, result.getPoolResults().size());
        assertTrue("总消耗资源应该大于0", result.getTotalConsumedResources() > 0);
    }

    @Test
    public void testMultiPoolSimulation_LoopStrategy() {
        LoopStrategy strategy = new LoopStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(5, context, stats);

        assertNotNull("结果不应为空", result);
        assertEquals("应该有5个池子的结果", 5, result.getPoolResults().size());
        assertTrue("总消耗资源应该大于0", result.getTotalConsumedResources() > 0);

        // 验证资源消耗
        assertTrue("总消耗资源应该大于0", result.getTotalConsumedResources() > 0);
        assertTrue("剩余资源应该非负", result.getFinalResources() >= 0);
    }

    // ==================== 统计信息测试 ====================

    @Test
    public void testStatisticsTracking() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        simulator.simulateMultiplePools(3, context, stats);

        // 验证统计追踪器记录了正确信息
        assertTrue("应该有记录的UP角色", stats.getObtainedUpCharacters().size() > 0);
        assertTrue("总6星数应该大于0", stats.getTotalSixStars() > 0);
    }

    @Test
    public void testUniqueUpCount() {
        HardPityStrategy strategy = new HardPityStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(5, context, stats);

        // 不同UP角色数应该小于等于当期UP总数
        assertTrue("不同UP角色数应该合理",
                result.getUniqueUpCount() <= result.getTotalCurrentUp());
    }

    // ==================== 资源管理测试 ====================

    @Test
    public void testResourceConsumption() {
        LoopStrategy strategy = new LoopStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        // simulateMultiplePools会自动调用enterNextPool
        // 3个池子：第1池120 + 第2池80 + 第3池80 = 280
        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(3, context, stats);

        // 验证资源被正确消耗
        assertTrue("应该有资源消耗", result.getTotalConsumedResources() > 0);
        // 初始120 + 第2池80 + 第3池80 = 280
        // 消耗 + 剩余 = 总获得资源
        int totalGained = 120 + 80 + 80; // 280
        assertEquals("消耗+剩余应该等于总获得资源",
                totalGained,
                result.getTotalConsumedResources() + result.getFinalResources());
    }

    @Test
    public void testResourceDepletion() {
        // 测试资源耗尽的情况
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        // 模拟很多个池子，直到资源耗尽
        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(10, context, stats);

        // 验证资源管理正确
        assertTrue("剩余资源应该非负", result.getFinalResources() >= 0);
    }

    // ==================== 批量模拟统计测试 ====================

    @Test
    public void testBatchSimulationStatistics() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        GachaSimulator.MultiPoolStatisticsResult result =
                GachaSimulator.compareStrategiesMultiPool(
                        java.util.Collections.singletonList(strategy),
                        pool,
                        100,  // 100次模拟
                        3     // 3个池子
                ).get(strategy.getStrategyName());

        assertNotNull("结果不应为空", result);
        assertEquals("模拟次数应该正确", 100, result.getTotalSimulations());
        assertTrue("平均6星数应该大于0", result.getAverageSixStars() > 0);
        assertTrue("平均UP数应该大于0", result.getAverageCurrentUp() > 0);
    }
}
