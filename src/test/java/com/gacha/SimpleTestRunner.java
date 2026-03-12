package com.gacha;

import com.gacha.engine.GachaEngine;
import com.gacha.model.Card;
import com.gacha.model.CardPool;
import com.gacha.model.Rarity;
import com.gacha.resource.ResourceManager;
import com.gacha.simulator.GachaSimulator;
import com.gacha.statistics.StatisticsTracker;
import com.gacha.strategy.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单测试运行器 - 不使用JUnit，直接验证功能
 */
public class SimpleTestRunner {

    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       抽卡模拟器简单测试运行器               ");
        System.out.println("==============================================\n");

        // 测试资源管理器
        System.out.println("【测试 ResourceManager】");
        testResourceManager();

        // 测试策略
        System.out.println("\n【测试 DrawStrategy】");
        testStrategies();

        // 测试GachaEngine
        System.out.println("\n【测试 GachaEngine】");
        testGachaEngine();

        // 测试模拟器
        System.out.println("\n【测试 GachaSimulator】");
        testSimulator();

        // 打印结果
        System.out.println("\n==============================================");
        System.out.println("测试完成");
        System.out.println("==============================================");
        System.out.println("通过: " + passedTests);
        System.out.println("失败: " + failedTests);

        if (failedTests > 0) {
            System.out.println("\n失败的测试:");
            for (String name : failedTestNames) {
                System.out.println("  - " + name);
            }
        }

        System.out.println("\n结果: " + (failedTests == 0 ? "全部通过 ✓" : "存在失败 ✗"));
    }

    private static void testResourceManager() {
        ResourceManager rm = new ResourceManager();

        // 测试1: 初始状态
        assertTest("ResourceManager_初始状态",
                rm.getAvailableResources() == 0 && rm.getTotalConsumed() == 0);

        // 测试2: 第一个池子
        rm.enterNextPool();
        assertTest("ResourceManager_第一个池子",
                rm.getAvailableResources() == 120 && rm.getPoolCount() == 1);

        // 测试3: 消耗资源
        boolean success = rm.tryConsume(50);
        assertTest("ResourceManager_消耗50",
                success && rm.getAvailableResources() == 70 && rm.getTotalConsumed() == 50);

        // 测试4: 第二个池子
        rm.enterNextPool();
        assertTest("ResourceManager_第二个池子",
                rm.getAvailableResources() == 150 && rm.getPoolCount() == 2); // 70 + 80

        // 测试5: 资源不足
        boolean fail = rm.tryConsume(200);
        assertTest("ResourceManager_资源不足",
                !fail);
    }

    private static void testStrategies() {
        CardPool pool = CardPool.createDefaultPool();

        // 测试 OneSixStarStrategy
        System.out.println("  测试 OneSixStarStrategy...");
        testOneSixStarStrategy(pool);

        // 测试 HardPityStrategy
        System.out.println("  测试 HardPityStrategy...");
        testHardPityStrategy(pool);

        // 测试 LoopStrategy
        System.out.println("  测试 LoopStrategy...");
        testLoopStrategy(pool);
    }

    private static void testOneSixStarStrategy(CardPool pool) {
        OneSixStarStrategy strategy = new OneSixStarStrategy();
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        // 测试1: 没有6星时继续
        boolean continue1 = strategy.shouldContinue(null, context, stats);
        assertTest("OneSixStarStrategy_无6星继续", continue1);

        // 测试2: 抽到6星后停止
        Card sixStar = Card.builder().id("6star").name("6星").rarity(Rarity.SIX_STAR).build();
        stats.recordCard(sixStar, false);
        boolean continue2 = strategy.shouldContinue(sixStar, context, stats);
        assertTest("OneSixStarStrategy_有6星停止", !continue2);

        // 测试3: 资源不足停止
        DrawContext context2 = new DrawContext();
        context2.enterNextPool();
        while (context2.hasEnoughResources(1)) {
            context2.tryConsumeResources(1);
        }
        boolean continue3 = strategy.shouldContinue(null, context2, stats);
        assertTest("OneSixStarStrategy_资源不足停止", !continue3);
    }

    private static void testHardPityStrategy(CardPool pool) {
        HardPityStrategy strategy = new HardPityStrategy();
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        // 测试1: 100抽时继续
        for (int i = 0; i < 100; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }
        boolean continue1 = strategy.shouldContinue(null, context, stats);
        assertTest("HardPityStrategy_100抽继续", continue1);

        // 测试2: 120抽时停止
        for (int i = 0; i < 20; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }
        boolean continue2 = strategy.shouldContinue(null, context, stats);
        assertTest("HardPityStrategy_120抽停止", !continue2);

        // 测试3: 拿到UP后停止
        DrawContext context2 = new DrawContext();
        context2.enterNextPool();
        for (int i = 0; i < 50; i++) {
            context2.incrementPaidDraws(1);
            context2.tryConsumeResources(1);
        }
        String upId = pool.getCurrentUpCharacters().get(0).getId();
        Card upCard = Card.builder().id(upId).name("UP").rarity(Rarity.SIX_STAR).build();
        stats.recordCard(upCard, true);
        boolean continue3 = strategy.shouldContinue(upCard, context2, stats);
        assertTest("HardPityStrategy_拿到UP停止", !continue3);
    }

    private static void testLoopStrategy(CardPool pool) {
        LoopStrategy strategy = new LoopStrategy();

        // 测试1: 第一个池子（120+10=130 >= 120）目标120抽
        DrawContext context1 = new DrawContext();
        StatisticsTracker stats1 = new StatisticsTracker();
        context1.enterNextPool();
        boolean continue1 = strategy.shouldContinue(null, context1, stats1);
        assertTest("LoopStrategy_第一池开始继续", continue1);

        // 抽到119抽时继续（同时消耗资源）
        for (int i = 0; i < 119; i++) {
            context1.incrementPaidDraws(1);
            context1.tryConsumeResources(1);
        }
        boolean continueAt119 = strategy.shouldContinue(null, context1, stats1);
        assertTest("LoopStrategy_第一池119抽继续", continueAt119);

        // 120抽时停止
        context1.incrementPaidDraws(1);
        context1.tryConsumeResources(1);
        boolean continueAt120 = strategy.shouldContinue(null, context1, stats1);
        assertTest("LoopStrategy_第一池120抽停止", !continueAt120);

        // 测试2: 第二个池子（剩余资源 + 80 + 10 = ?）
        DrawContext context2 = new DrawContext();
        StatisticsTracker stats2 = new StatisticsTracker();
        context2.enterNextPool(); // 第一池：120资源

        // 模拟第一池消耗100资源
        for (int i = 0; i < 100; i++) {
            context2.incrementPaidDraws(1);
            context2.tryConsumeResources(1);
        }

        context2.enterNextPool(); // 第二池：剩余20 + 80 = 100资源

        // 第二池初始资源应该是100（20+80），+ 免费10抽 = 110 < 120，目标30抽
        // 抽到29抽时继续
        for (int i = 0; i < 29; i++) {
            context2.incrementPaidDraws(1);
            context2.tryConsumeResources(1);
        }
        boolean continueAt29 = strategy.shouldContinue(null, context2, stats2);
        assertTest("LoopStrategy_第二池29抽继续", continueAt29);

        // 30抽时停止
        context2.incrementPaidDraws(1);
        context2.tryConsumeResources(1);
        boolean continueAt30 = strategy.shouldContinue(null, context2, stats2);
        assertTest("LoopStrategy_第二池30抽停止", !continueAt30);
    }

    private static void testGachaEngine() {
        CardPool pool = CardPool.createDefaultPool();
        GachaEngine engine = new GachaEngine(pool);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();
        context.enterNextPool();

        // 测试1: 单次抽卡
        Card card = engine.draw(context);
        assertTest("GachaEngine_单次抽卡", card != null && card.getRarity() != null);

        // 测试2: 保底计数器
        int pityBefore = context.getPityCounter();
        engine.draw(context);
        assertTest("GachaEngine_保底计数增加", context.getPityCounter() == pityBefore + 1);

        // 测试3: 6星概率（统计测试，可能偶尔失败）
        int sixStarCount = 0;
        DrawContext ctx = new DrawContext();
        ctx.enterNextPool();
        for (int i = 0; i < 1000; i++) {
            Card c = engine.draw(ctx);
            if (c.getRarity() == Rarity.SIX_STAR) sixStarCount++;
        }
        double rate = (double) sixStarCount / 1000;
        assertTest("GachaEngine_6星概率合理", rate >= 0.005 && rate <= 0.03);
    }

    private static void testSimulator() {
        CardPool pool = CardPool.createDefaultPool();

        // 测试1: 单池模拟 - OneSixStarStrategy
        OneSixStarStrategy strategy1 = new OneSixStarStrategy();
        GachaSimulator simulator1 = new GachaSimulator(pool, strategy1);
        DrawContext context1 = new DrawContext();
        StatisticsTracker stats1 = new StatisticsTracker();
        context1.enterNextPool();
        GachaSimulator.SingleSimulationResult result1 = simulator1.simulate(context1, stats1);
        assertTest("Simulator_OneSixStar单池",
                result1 != null && result1.getTotalDraws() > 0 && result1.getSixStarCount() >= 1);

        // 测试2: 单池模拟 - HardPityStrategy
        HardPityStrategy strategy2 = new HardPityStrategy();
        GachaSimulator simulator2 = new GachaSimulator(pool, strategy2);
        DrawContext context2 = new DrawContext();
        StatisticsTracker stats2 = new StatisticsTracker();
        context2.enterNextPool();
        GachaSimulator.SingleSimulationResult result2 = simulator2.simulate(context2, stats2);
        assertTest("Simulator_HardPity单池",
                result2 != null && result2.getPaidDraws() <= 120);

        // 测试3: 多池模拟
        LoopStrategy strategy3 = new LoopStrategy();
        GachaSimulator simulator3 = new GachaSimulator(pool, strategy3);
        DrawContext context3 = new DrawContext();
        StatisticsTracker stats3 = new StatisticsTracker();
        GachaSimulator.MultiPoolSimulationResult result3 = simulator3.simulateMultiplePools(3, context3, stats3);
        assertTest("Simulator_LoopStrategy多池",
                result3 != null && result3.getPoolResults().size() == 3);
    }

    private static void assertTest(String testName, boolean condition) {
        if (condition) {
            passedTests++;
            System.out.println("  ✓ " + testName);
        } else {
            failedTests++;
            failedTestNames.add(testName);
            System.out.println("  ✗ " + testName);
        }
    }
}
