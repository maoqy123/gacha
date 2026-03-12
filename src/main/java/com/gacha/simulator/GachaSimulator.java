package com.gacha.simulator;

import com.gacha.engine.GachaEngine;
import com.gacha.model.Card;
import com.gacha.model.CardPool;
import com.gacha.model.Rarity;
import com.gacha.statistics.StatisticsTracker;
import com.gacha.strategy.DrawContext;
import com.gacha.strategy.DrawStrategy;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽卡模拟器核心类 - 重构后版本
 * 职责分离：
 * - GachaEngine: 处理抽卡规则和概率计算
 * - DrawStrategy: 只负责判断是否继续抽卡
 * - StatisticsTracker: 处理统计记录
 */
public class GachaSimulator {

    private final CardPool cardPool;
    private final DrawStrategy strategy;
    private final GachaEngine engine;

    public GachaSimulator(CardPool cardPool, DrawStrategy strategy) {
        this.cardPool = cardPool;
        this.strategy = strategy;
        this.engine = new GachaEngine(cardPool);
    }

    /**
     * 执行一次模拟（单池）
     * @param context 抽卡上下文
     * @param stats 统计追踪器
     * @return 模拟结果
     */
    public SingleSimulationResult simulate(DrawContext context, StatisticsTracker stats) {
        List<Card> allCards = new ArrayList<>();
        Card lastCard = null;
        boolean lastIsCurrentUp = false;

        // 策略决定是否继续抽卡
        while (strategy.shouldContinue(lastCard, context, stats, lastIsCurrentUp)) {
            // 检查资源
            if (!context.hasEnoughResources(1)) {
                break;
            }

            // 执行抽卡
            Card card = engine.draw(context);
            allCards.add(card);

            // 更新状态
            context.incrementTotalDraws(1);
            context.incrementPaidDraws(1);
            context.tryConsumeResources(1);

            // 记录统计
            boolean isUpCharacter = engine.isUpCharacter(card);
            stats.recordCard(card, isUpCharacter);

            // 判断是否是当期UP
            lastIsCurrentUp = engine.isCurrentUp(card);
            lastCard = card;
        }

        return SingleSimulationResult.builder()
                .cards(allCards)
                .totalDraws(context.getTotalDraws())
                .paidDraws(context.getPaidDraws())
                .pityCounter(context.getPityCounter())
                .rarityDistribution(stats.getRarityDistribution())
                .bonusRecords(context.getBonusRecords())
                .availableResources(context.getAvailableResources())
                .totalConsumedResources(context.getResourceManager() != null ?
                        context.getResourceManager().getTotalConsumed() : 0)
                .newUpCount(stats.getCurrentPoolNewUpCount())
                .duplicateUpCount(stats.getCurrentPoolDuplicateUpCount())
                .build();
    }

    /**
     * 执行多池模拟
     * @param poolCount 池子数量
     * @param context 抽卡上下文
     * @param stats 统计追踪器
     * @return 多池模拟结果
     */
    public MultiPoolSimulationResult simulateMultiplePools(int poolCount, DrawContext context, StatisticsTracker stats) {
        List<SingleSimulationResult> poolResults = new ArrayList<>();

        for (int i = 0; i < poolCount; i++) {
            // 更新卡池期数
            cardPool.setCurrentPeriod(i + 1);

            // 进入下一个池子
            context.enterNextPool();
            stats.enterNextPool();

            // 执行当前池子的抽卡
            SingleSimulationResult result = simulate(context, stats);
            poolResults.add(result);
        }

        return MultiPoolSimulationResult.builder()
                .poolResults(poolResults)
                .totalPools(poolCount)
                .totalSixStars(stats.getTotalSixStars())
                .totalCurrentUp(stats.getTotalCurrentUp())
                .totalNewUp(stats.getTotalNewUp())
                .totalDuplicateUp(stats.getTotalDuplicateUp())
                .finalResources(context.getAvailableResources())
                .totalConsumedResources(context.getResourceManager() != null ?
                        context.getResourceManager().getTotalConsumed() : 0)
                .uniqueUpCount(stats.getUniqueUpCount())
                .build();
    }

    /**
     * 批量模拟运行（单池）
     * @param simulationCount 模拟次数
     * @return 统计结果列表
     */
    public List<SingleSimulationResult> runBatchSimulation(int simulationCount) {
        List<SingleSimulationResult> results = new ArrayList<>();

        for (int i = 0; i < simulationCount; i++) {
            DrawContext context = new DrawContext();
            StatisticsTracker stats = new StatisticsTracker();

            // 进入第一个池子
            context.enterNextPool();

            SingleSimulationResult result = simulate(context, stats);
            results.add(result);
        }

        return results;
    }

    /**
     * 批量多池模拟
     * @param simulationCount 模拟次数
     * @param poolCount 每个模拟的池子数量
     * @return 多池统计结果
     */
    public MultiPoolStatisticsResult runBatchMultiPoolSimulation(int simulationCount, int poolCount) {
        List<MultiPoolSimulationResult> results = new ArrayList<>();

        for (int i = 0; i < simulationCount; i++) {
            DrawContext context = new DrawContext();
            StatisticsTracker stats = new StatisticsTracker();

            MultiPoolSimulationResult result = simulateMultiplePools(poolCount, context, stats);
            results.add(result);
        }

        return analyzeMultiPoolResults(results);
    }

    /**
     * 分析多池模拟结果
     */
    private MultiPoolStatisticsResult analyzeMultiPoolResults(List<MultiPoolSimulationResult> results) {
        int totalSimulations = results.size();

        // 计算平均值
        double avgSixStars = results.stream()
                .mapToInt(MultiPoolSimulationResult::getTotalSixStars)
                .average().orElse(0);

        double avgCurrentUp = results.stream()
                .mapToInt(MultiPoolSimulationResult::getTotalCurrentUp)
                .average().orElse(0);

        double avgNewUp = results.stream()
                .mapToInt(MultiPoolSimulationResult::getTotalNewUp)
                .average().orElse(0);

        double avgDuplicateUp = results.stream()
                .mapToInt(MultiPoolSimulationResult::getTotalDuplicateUp)
                .average().orElse(0);

        double avgUniqueUp = results.stream()
                .mapToInt(MultiPoolSimulationResult::getUniqueUpCount)
                .average().orElse(0);

        double avgConsumedResources = results.stream()
                .mapToInt(MultiPoolSimulationResult::getTotalConsumedResources)
                .average().orElse(0);

        double avgFinalResources = results.stream()
                .mapToInt(MultiPoolSimulationResult::getFinalResources)
                .average().orElse(0);

        return MultiPoolStatisticsResult.builder()
                .totalSimulations(totalSimulations)
                .averageSixStars(avgSixStars)
                .averageCurrentUp(avgCurrentUp)
                .averageNewUp(avgNewUp)
                .averageDuplicateUp(avgDuplicateUp)
                .averageUniqueUp(avgUniqueUp)
                .averageConsumedResources(avgConsumedResources)
                .averageFinalResources(avgFinalResources)
                .build();
    }

    /**
     * 对比不同策略（多池）
     * @param strategies 要对比的策略列表
     * @param pool 卡池
     * @param simulationCount 每种策略的模拟次数
     * @param poolCount 每个模拟的池子数量
     * @return 对比结果
     */
    public static Map<String, MultiPoolStatisticsResult> compareStrategiesMultiPool(
            List<DrawStrategy> strategies,
            CardPool pool,
            int simulationCount,
            int poolCount) {

        Map<String, MultiPoolStatisticsResult> comparison = new HashMap<>();

        for (DrawStrategy strategy : strategies) {
            GachaSimulator simulator = new GachaSimulator(pool, strategy);
            MultiPoolStatisticsResult result = simulator.runBatchMultiPoolSimulation(simulationCount, poolCount);
            comparison.put(strategy.getStrategyName(), result);
        }

        return comparison;
    }

    /**
     * 单次模拟结果
     */
    @Data
    @Builder
    public static class SingleSimulationResult {
        private int totalDraws;  // 总抽卡次数（含赠送）
        private int paidDraws;   // 实际付费抽卡次数
        private List<Card> cards;
        private Map<Rarity, Integer> rarityDistribution;
        private int pityCounter;
        private List<String> bonusRecords;
        private int availableResources;  // 剩余资源
        private int totalConsumedResources;  // 总消耗资源
        private int newUpCount;      // 新UP数量
        private int duplicateUpCount; // 重复UP数量

        public int getSixStarCount() {
            return rarityDistribution.getOrDefault(Rarity.SIX_STAR, 0);
        }

        public int getSSRCount() {
            return rarityDistribution.getOrDefault(Rarity.SSR, 0);
        }

        public int getSRCount() {
            return rarityDistribution.getOrDefault(Rarity.SR, 0);
        }

        public int getCurrentUpCount() {
            return newUpCount + duplicateUpCount;
        }

        public int getEffectiveUpCount() {
            return newUpCount;
        }

        public int getWastedUpCount() {
            return duplicateUpCount;
        }
    }

    /**
     * 多池模拟结果
     */
    @Data
    @Builder
    public static class MultiPoolSimulationResult {
        private List<SingleSimulationResult> poolResults;
        private int totalPools;
        private int totalSixStars;
        private int totalCurrentUp;
        private int totalNewUp;
        private int totalDuplicateUp;
        private int finalResources;
        private int totalConsumedResources;
        private int uniqueUpCount;  // 不同UP角色的数量
    }

    /**
     * 多池统计结果
     */
    @Data
    @Builder
    public static class MultiPoolStatisticsResult {
        private int totalSimulations;
        private double averageSixStars;
        private double averageCurrentUp;
        private double averageNewUp;
        private double averageDuplicateUp;
        private double averageUniqueUp;
        private double averageConsumedResources;
        private double averageFinalResources;

        public String formatReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================\n");
            sb.append("        多池模拟统计报告              \n");
            sb.append("========================================\n\n");

            sb.append(String.format("总模拟次数: %d\n", totalSimulations));
            sb.append(String.format("平均获得6星: %.2f 个\n", averageSixStars));
            sb.append(String.format("平均获得当期UP: %.2f 个\n", averageCurrentUp));
            sb.append(String.format("  - 新UP(有效): %.2f 个\n", averageNewUp));
            sb.append(String.format("  - 重复UP(无效): %.2f 个\n", averageDuplicateUp));
            sb.append(String.format("平均获得不同UP角色: %.2f 个\n", averageUniqueUp));
            sb.append(String.format("平均消耗资源: %.2f 抽\n", averageConsumedResources));
            sb.append(String.format("平均剩余资源: %.2f 抽\n", averageFinalResources));

            return sb.toString();
        }
    }
}
