package com.gacha.statistics;

import com.gacha.model.Rarity;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

/**
 * 模拟统计结果
 */
@Data
@Builder
public class SimulationResult {

    // 基础统计
    private int totalSimulations;
    private int targetSuccessCount;   // 成功获得目标的次数

    // 抽卡次数统计
    private double averageDrawsToTarget;
    private double medianDrawsToTarget;
    private int minDrawsToTarget;
    private int maxDrawsToTarget;
    private double percentile90;      // 90%分位数
    private double percentile95;      // 95%分位数
    private double percentile99;      // 99%分位数

    // 稀有度分布（平均值）
    private Map<Rarity, Double> averageRarityDistribution;

    // 资源消耗统计
    private double averageCost;       // 平均消耗（假设每抽一定价格）

    // 概率分析
    private double successRate;       // 成功率
    private double expectedValue;     // 期望值

    // 额外统计
    private double averagePityCounter;  // 平均保底计数
    private double averageBonusCount;   // 平均赠送次数

    /**
     * 格式化输出结果
     */
    public String formatReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          抽卡模拟统计报告              \n");
        sb.append("========================================\n\n");

        sb.append(String.format("总模拟次数: %d\n", totalSimulations));
        sb.append(String.format("成功获得目标: %d 次 (%.2f%%)\n\n",
                targetSuccessCount, successRate * 100));

        sb.append("--- 抽卡次数统计 ---\n");
        sb.append(String.format("平均抽卡次数: %.2f\n", averageDrawsToTarget));
        sb.append(String.format("中位数: %.2f\n", medianDrawsToTarget));
        sb.append(String.format("最少次数: %d\n", minDrawsToTarget));
        sb.append(String.format("最多次数: %d\n", maxDrawsToTarget));
        sb.append(String.format("90%%分位数: %.2f (90%%玩家在%.0f抽内获得)\n", percentile90, percentile90));
        sb.append(String.format("95%%分位数: %.2f\n", percentile95));
        sb.append(String.format("99%%分位数: %.2f\n\n", percentile99));

        sb.append("--- 稀有度分布（平均每次模拟） ---\n");
        if (averageRarityDistribution != null) {
            for (Rarity rarity : Rarity.values()) {
                double count = averageRarityDistribution.getOrDefault(rarity, 0.0);
                sb.append(String.format("%s: %.2f\n", rarity.getDisplayName(), count));
            }
        }

        sb.append("\n--- 其他统计 ---\n");
        sb.append(String.format("平均保底计数: %.2f\n", averagePityCounter));
        sb.append(String.format("平均赠送次数: %.2f\n", averageBonusCount));

        sb.append("\n--- 期望值分析 ---\n");
        sb.append(String.format("期望抽卡次数: %.2f\n", expectedValue));

        return sb.toString();
    }
}
