package com.gacha;

import com.gacha.model.CardPool;
import com.gacha.simulator.GachaSimulator;
import com.gacha.strategy.DrawStrategy;
import com.gacha.strategy.HardPityStrategy;
import com.gacha.strategy.LoopStrategy;
import com.gacha.strategy.OneSixStarStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 策略对比统计测试
 * 每个策略抽20个卡池，重复1000次，输出平均期望
 */
public class StrategyStatisticsTest {

    private static final int SIMULATION_COUNT = 100;
    private static final int POOL_COUNT = 20;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       抽卡策略统计对比测试");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("配置:");
        System.out.println("  - 模拟次数: " + SIMULATION_COUNT);
        System.out.println("  - 卡池数量: " + POOL_COUNT);
        System.out.println("  - 资源配置: 第1期120石, 第2-3期各80石, 第4期及以后各60石");
        System.out.println("  - 总资源: 1280石");
        System.out.println();

        // 创建卡池
        CardPool cardPool = CardPool.createDefaultPool();

        // 创建策略列表
        List<DrawStrategy> strategies = new ArrayList<>();
        strategies.add(new OneSixStarStrategy());
        strategies.add(new HardPityStrategy());
        strategies.add(new LoopStrategy());

        // 运行对比
        System.out.println("正在运行模拟...");
        System.out.println();

        Map<String, GachaSimulator.MultiPoolStatisticsResult> results =
                GachaSimulator.compareStrategiesMultiPool(strategies, cardPool, SIMULATION_COUNT, POOL_COUNT);

        // 输出结果
        System.out.println("==============================================");
        System.out.println("              统计结果");
        System.out.println("==============================================");
        System.out.println();

        for (Map.Entry<String, GachaSimulator.MultiPoolStatisticsResult> entry : results.entrySet()) {
            String strategyName = entry.getKey();
            GachaSimulator.MultiPoolStatisticsResult stats = entry.getValue();

            System.out.println("【" + strategyName + "】");
            System.out.println("──────────────────────────────────────────────");
            System.out.println(String.format("  平均获得6星:     %.2f 个", stats.getAverageSixStars()));
            System.out.println(String.format("  平均获得当期UP:   %.2f 个", stats.getAverageCurrentUp()));
            System.out.println(String.format("    - 有效新UP:    %.2f 个", stats.getAverageNewUp()));
            System.out.println(String.format("    - 重复UP:      %.2f 个", stats.getAverageDuplicateUp()));
            System.out.println(String.format("  平均不同UP角色:   %.2f 个", stats.getAverageUniqueUp()));
            System.out.println(String.format("  平均消耗石头:     %.2f 石", stats.getAverageConsumedResources()));
            System.out.println(String.format("  平均剩余石头:     %.2f 石", stats.getAverageFinalResources()));
            System.out.println();
        }

        // 输出对比表格
        System.out.println("==============================================");
        System.out.println("              策略对比表");
        System.out.println("==============================================");
        System.out.println();
        System.out.printf("%-20s %8s %8s %8s %8s%n",
                "策略", "有效UP", "不同UP", "消耗石", "剩余石");
        System.out.println("──────────────────────────────────────────────");

        for (Map.Entry<String, GachaSimulator.MultiPoolStatisticsResult> entry : results.entrySet()) {
            String strategyName = entry.getKey();
            GachaSimulator.MultiPoolStatisticsResult stats = entry.getValue();

            System.out.printf("%-20s %8.2f %8.2f %8.2f %8.2f%n",
                    strategyName,
                    stats.getAverageNewUp(),
                    stats.getAverageUniqueUp(),
                    stats.getAverageConsumedResources(),
                    stats.getAverageFinalResources());
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("              效率分析");
        System.out.println("==============================================");
        System.out.println();

        for (Map.Entry<String, GachaSimulator.MultiPoolStatisticsResult> entry : results.entrySet()) {
            String strategyName = entry.getKey();
            GachaSimulator.MultiPoolStatisticsResult stats = entry.getValue();

            double upPerStone = stats.getAverageNewUp() / stats.getAverageConsumedResources() * 100;
            double uniqueUpRate = stats.getAverageUniqueUp() / stats.getAverageNewUp() * 100;

            System.out.println("【" + strategyName + "】");
            System.out.println(String.format("  每100石有效UP:   %.2f 个", upPerStone));
            System.out.println(String.format("  有效UP利用率:    %.1f%%", uniqueUpRate));
            System.out.println();
        }
    }
}
