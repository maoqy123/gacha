package com.gacha.strategy;

import com.gacha.model.Card;
import com.gacha.model.CardPool;
import com.gacha.model.Rarity;
import com.gacha.statistics.StatisticsTracker;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 抽卡策略测试类 - 测试各种策略的行为
 */
public class StrategyTest {

    private CardPool pool;
    private DrawContext context;
    private StatisticsTracker stats;

    @Before
    public void setUp() {
        pool = CardPool.createDefaultPool();
        context = new DrawContext();
        stats = new StatisticsTracker();
        // 进入第一个池子
        context.enterNextPool();
    }

    // ==================== OneSixStarStrategy 测试 ====================

    @Test
    public void testOneSixStarStrategy_ContinueBeforeSixStar() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();

        // 没有抽到6星时应该继续
        boolean shouldContinue = strategy.shouldContinue(null, context, stats);
        assertTrue("没有6星时应该继续抽卡", shouldContinue);
    }

    @Test
    public void testOneSixStarStrategy_StopAfterCurrentUp() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();

        // 创建一个当期UP卡牌
        String upId = pool.getCurrentUpCharacters().get(0).getId();
        Card upCard = Card.builder()
                .id(upId)
                .name("当期UP")
                .rarity(Rarity.SIX_STAR)
                .build();

        // 抽到当期UP后应该停止
        boolean shouldContinue = strategy.shouldContinue(upCard, context, stats, true);
        assertFalse("抽到当期UP后应该停止", shouldContinue);
    }

    @Test
    public void testOneSixStarStrategy_ContinueAfterOffBannerWithResources() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();

        // 创建一个非当期UP的6星卡牌（歪了）
        Card offBannerCard = Card.builder()
                .id("off-banner-6star")
                .name("歪了的6星")
                .rarity(Rarity.SIX_STAR)
                .build();

        // 有足够资源（120石），歪了应该继续
        boolean shouldContinue = strategy.shouldContinue(offBannerCard, context, stats, false);
        assertTrue("歪了且有65抽资源应该继续", shouldContinue);
    }

    @Test
    public void testOneSixStarStrategy_StopAfterOffBannerWithoutResources() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();

        // 消耗大部分资源，只留60抽（不够软保底65抽）
        for (int i = 0; i < 60; i++) {
            context.tryConsumeResources(1);
        }

        // 创建一个非当期UP的6星卡牌（歪了）
        Card offBannerCard = Card.builder()
                .id("off-banner-6star")
                .name("歪了的6星")
                .rarity(Rarity.SIX_STAR)
                .build();

        // 没有足够资源（只剩60抽），歪了应该停止
        boolean shouldContinue = strategy.shouldContinue(offBannerCard, context, stats, false);
        assertFalse("歪了且没有65抽资源应该停止", shouldContinue);
    }

    @Test
    public void testOneSixStarStrategy_StopWhenNoResources() {
        OneSixStarStrategy strategy = new OneSixStarStrategy();

        // 消耗所有资源
        while (context.hasEnoughResources(1)) {
            context.tryConsumeResources(1);
        }

        // 资源不足时应该停止
        boolean shouldContinue = strategy.shouldContinue(null, context, stats);
        assertFalse("资源不足时应该停止", shouldContinue);
    }

    // ==================== HardPityStrategy 测试 ====================

    @Test
    public void testHardPityStrategy_ContinueBefore120() {
        HardPityStrategy strategy = new HardPityStrategy();

        // 抽到100抽时应该继续
        for (int i = 0; i < 100; i++) {
            context.incrementPaidDraws(1);
        }

        boolean shouldContinue = strategy.shouldContinue(null, context, stats);
        assertTrue("100抽时应该继续", shouldContinue);
    }

    @Test
    public void testHardPityStrategy_StopAt120() {
        HardPityStrategy strategy = new HardPityStrategy();

        // 抽到120抽时应该停止
        for (int i = 0; i < 120; i++) {
            context.incrementPaidDraws(1);
        }

        boolean shouldContinue = strategy.shouldContinue(null, context, stats);
        assertFalse("120抽时应该停止", shouldContinue);
    }

    @Test
    public void testHardPityStrategy_StopWhenGetCurrentUp() {
        HardPityStrategy strategy = new HardPityStrategy();

        // 只抽了50抽就拿到当期UP
        for (int i = 0; i < 50; i++) {
            context.incrementPaidDraws(1);
        }

        // 记录当期UP
        String upId = pool.getCurrentUpCharacters().get(0).getId();
        Card upCard = Card.builder()
                .id(upId)
                .name("当期UP")
                .rarity(Rarity.SIX_STAR)
                .build();
        stats.recordCard(upCard, true);

        // 拿到当期UP后应该停止
        boolean shouldContinue = strategy.shouldContinue(upCard, context, stats);
        assertFalse("拿到当期UP后应该停止", shouldContinue);
    }

    // ==================== LoopStrategy 测试 ====================

    @Test
    public void testLoopStrategy_Target120WhenResourcesSufficient() {
        LoopStrategy strategy = new LoopStrategy();

        // 第一个池子初始120资源 + 本期免费10抽 = 130 >= 120
        // 应该目标120抽
        boolean shouldContinueAtStart = strategy.shouldContinue(null, context, stats);
        assertTrue("资源充足时应该继续", shouldContinueAtStart);

        // 抽到119抽时应该继续
        for (int i = 0; i < 119; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }
        boolean shouldContinueAt119 = strategy.shouldContinue(null, context, stats);
        assertTrue("119抽时应该继续", shouldContinueAt119);

        // 抽到120抽时应该停止
        context.incrementPaidDraws(1);
        context.tryConsumeResources(1);
        boolean shouldContinueAt120 = strategy.shouldContinue(null, context, stats);
        assertFalse("120抽时应该停止", shouldContinueAt120);
    }

    @Test
    public void testLoopStrategy_Target30WhenResourcesInsufficient() {
        LoopStrategy strategy = new LoopStrategy();

        // 模拟第一池消耗100资源
        for (int i = 0; i < 100; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }

        // 进入第二池：剩余20 + 80 = 100资源
        context.enterNextPool();

        // 第二池初始资源 = 100，+ 免费10抽 = 110 < 120，目标30抽
        // 抽到29抽时应该继续
        for (int i = 0; i < 29; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }
        boolean shouldContinueAt29 = strategy.shouldContinue(null, context, stats);
        assertTrue("29抽时应该继续", shouldContinueAt29);

        // 抽到30抽时应该停止
        context.incrementPaidDraws(1);
        context.tryConsumeResources(1);
        boolean shouldContinueAt30 = strategy.shouldContinue(null, context, stats);
        assertFalse("30抽时应该停止", shouldContinueAt30);
    }

    @Test
    public void testLoopStrategy_WithLastPoolBonus() {
        LoopStrategy strategy = new LoopStrategy();

        // 模拟第一池消耗100资源
        for (int i = 0; i < 100; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }

        // 设置有上期赠送
        context.setHasNextPoolFreeTenPull(true);
        context.enterNextPool(); // 进入第二个池子

        // 第二池初始资源 = 100，+ 上期赠送10 + 免费10抽 = 120 >= 120，目标120抽
        // 抽到29抽时应该继续
        for (int i = 0; i < 29; i++) {
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);
        }
        boolean shouldContinueAt29 = strategy.shouldContinue(null, context, stats);
        assertTrue("29抽时应该继续", shouldContinueAt29);

        // 抽到30抽时应该继续（因为目标120抽）
        context.incrementPaidDraws(1);
        context.tryConsumeResources(1);
        boolean shouldContinueAt30 = strategy.shouldContinue(null, context, stats);
        assertTrue("30抽时应该继续（目标120抽）", shouldContinueAt30);
    }

    @Test
    public void testLoopStrategy_StopWhenGetCurrentUp() {
        LoopStrategy strategy = new LoopStrategy();

        // 抽到50抽就拿到当期UP
        for (int i = 0; i < 50; i++) {
            context.incrementPaidDraws(1);
        }

        // 记录当期UP
        String upId = pool.getCurrentUpCharacters().get(0).getId();
        Card upCard = Card.builder()
                .id(upId)
                .name("当期UP")
                .rarity(Rarity.SIX_STAR)
                .build();
        stats.recordCard(upCard, true);

        // 拿到当期UP后应该停止
        boolean shouldContinue = strategy.shouldContinue(upCard, context, stats);
        assertFalse("拿到当期UP后应该停止", shouldContinue);
    }

    @Test
    public void testLoopStrategy_StopWhenNoResources() {
        LoopStrategy strategy = new LoopStrategy();

        // 消耗所有资源
        while (context.hasEnoughResources(1)) {
            context.tryConsumeResources(1);
        }

        // 资源不足时应该停止
        boolean shouldContinue = strategy.shouldContinue(null, context, stats);
        assertFalse("资源不足时应该停止", shouldContinue);
    }

    // ==================== 策略名称和描述测试 ====================

    @Test
    public void testStrategyNames() {
        assertEquals("抽到当期UP就收手", new OneSixStarStrategy().getStrategyName());
        assertEquals("120抽硬保底策略", new HardPityStrategy().getStrategyName());
        assertEquals("120+30循环策略", new LoopStrategy().getStrategyName());
    }

    @Test
    public void testStrategyDescriptions() {
        assertNotNull(new OneSixStarStrategy().getStrategyDescription());
        assertNotNull(new HardPityStrategy().getStrategyDescription());
        assertNotNull(new LoopStrategy().getStrategyDescription());

        assertFalse(new OneSixStarStrategy().getStrategyDescription().isEmpty());
        assertFalse(new HardPityStrategy().getStrategyDescription().isEmpty());
        assertFalse(new LoopStrategy().getStrategyDescription().isEmpty());
    }
}
