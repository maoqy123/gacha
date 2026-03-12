package com.gacha;

import com.gacha.model.CardPool;
import com.gacha.simulator.GachaSimulator;
import com.gacha.statistics.StatisticsTracker;
import com.gacha.strategy.DrawStrategy;
import com.gacha.strategy.OneSixStarStrategy;
import com.gacha.strategy.HardPityStrategy;
import com.gacha.strategy.LoopStrategy;
import com.gacha.strategy.DrawContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 抽卡模拟器入口类 - 重构后版本
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       抽卡策略模拟器 - Gacha Simulator       ");
        System.out.println("       (重构版 - 职责分离)                    ");
        System.out.println("==============================================\n");

        // 获取默认卡池配置
        CardPool pool = CardPool.createDefaultPool();
        System.out.println("卡池配置: " + pool.getPoolName());
        System.out.println(pool.getRule().getRuleDescription());
        System.out.println();

        // 显示架构说明
        System.out.println("架构说明:");
        System.out.println("  - GachaEngine: 处理抽卡规则和概率计算");
        System.out.println("  - DrawStrategy: 只负责判断是否继续抽卡");
        System.out.println("  - StatisticsTracker: 处理统计记录");
        System.out.println("  - DrawContext: 管理抽卡状态和资源");
        System.out.println();

        // 显示资源规则
        System.out.println("资源规则:");
        System.out.println("  - 第一个池子: 120抽资源");
        System.out.println("  - 后续池子: 每个80抽资源");
        System.out.println("  - 资源不足时策略会提前退出");
        System.out.println();

        // 显示长期统计规则
        System.out.println("长期统计规则:");
        System.out.println("  - 新UP: 首次获得的不同UP角色（有效）");
        System.out.println("  - 重复UP: 已经获得过的UP角色（无效）");
        System.out.println("  - 不同UP角色: 跨池统计去重后的UP数量");
        System.out.println();

        // 定义要测试的策略
        List<DrawStrategy> strategies = Arrays.asList(
                new OneSixStarStrategy(),      // 抽到一个6星就收手
                new HardPityStrategy(),        // 120抽硬保底
                new LoopStrategy()             // 120+30循环策略
        );

        int simulationCount = 1000;
        int poolCount = 5;  // 模拟5个池子

        System.out.println("开始多池模拟");
        System.out.println("模拟次数 = " + simulationCount);
        System.out.println("池子数量 = " + poolCount + "\n");

        // 运行多池对比测试
        Map<String, GachaSimulator.MultiPoolStatisticsResult> results = 
                GachaSimulator.compareStrategiesMultiPool(strategies, pool, simulationCount, poolCount);

        // 输出结果
        for (Map.Entry<String, GachaSimulator.MultiPoolStatisticsResult> entry : results.entrySet()) {
            System.out.println("\n========================================");
            System.out.println("策略: " + entry.getKey());
            System.out.println("========================================");
            System.out.println(entry.getValue().formatReport());
        }

        // 详细测试LoopStrategy
        System.out.println("\n\n========================================");
        System.out.println("详细测试 - 120+30循环策略（5个池子）");
        System.out.println("========================================\n");

        LoopStrategy loopStrategy = new LoopStrategy();
        GachaSimulator loopSimulator = new GachaSimulator(pool, loopStrategy);

        for (int i = 0; i < 3; i++) {
            DrawContext context = new DrawContext();
            StatisticsTracker stats = new StatisticsTracker();
            GachaSimulator.MultiPoolSimulationResult result = loopSimulator.simulateMultiplePools(5, context, stats);
            
            System.out.println("第" + (i + 1) + "次模拟:");
            System.out.println("  总6星数: " + result.getTotalSixStars());
            System.out.println("  总当期UP: " + result.getTotalCurrentUp() + " (新UP: " + result.getTotalNewUp() + ", 重复UP: " + result.getTotalDuplicateUp() + ")");
            System.out.println("  不同UP角色数: " + result.getUniqueUpCount());
            System.out.println("  总消耗资源: " + result.getTotalConsumedResources());
            System.out.println("  剩余资源: " + result.getFinalResources());
            System.out.println("  各池子详情:");
            
            for (int j = 0; j < result.getPoolResults().size(); j++) {
                GachaSimulator.SingleSimulationResult poolResult = result.getPoolResults().get(j);
                String target = poolResult.getPaidDraws() >= 100 ? "[120目标]" : "[30目标]";
                System.out.println("    池子" + (j + 1) + target + ": " + poolResult.getPaidDraws() + "抽(付费), " + 
                        poolResult.getSixStarCount() + "个6星, " + 
                        poolResult.getNewUpCount() + "新UP, " + poolResult.getDuplicateUpCount() + "重复UP");
            }
            System.out.println();
        }
    }
}
