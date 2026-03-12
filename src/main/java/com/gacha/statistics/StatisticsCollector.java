package com.gacha.statistics;

import com.gacha.model.Rarity;
import com.gacha.simulator.GachaSimulator;

import java.util.*;

/**
 * 统计数据收集器
 */
public class StatisticsCollector {

    /**
     * 分析模拟结果
     */
    public SimulationResult analyze(List<GachaSimulator.SingleSimulationResult> results) {
        int totalSimulations = results.size();

        // 收集抽卡次数数据
        List<Integer> drawCounts = new ArrayList<>();

        // 稀有度统计
        Map<Rarity, List<Integer>> rarityCounts = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            rarityCounts.put(rarity, new ArrayList<>());
        }

        // 保底统计
        List<Integer> pityCounters = new ArrayList<>();

        // 赠送统计
        int totalBonusCount = 0;

        for (GachaSimulator.SingleSimulationResult result : results) {
            drawCounts.add(result.getTotalDraws());

            // 收集稀有度分布
            for (Rarity rarity : Rarity.values()) {
                int count = result.getRarityDistribution().getOrDefault(rarity, 0);
                rarityCounts.get(rarity).add(count);
            }

            // 收集保底计数
            pityCounters.add(result.getPityCounter());

            // 统计赠送次数
            if (result.getBonusRecords() != null) {
                totalBonusCount += result.getBonusRecords().size();
            }
        }

        // 计算统计数据
        double avgDraws = calculateAverage(drawCounts);
        double medianDraws = calculateMedian(drawCounts);
        int minDraws = drawCounts.isEmpty() ? 0 : Collections.min(drawCounts);
        int maxDraws = drawCounts.isEmpty() ? 0 : Collections.max(drawCounts);

        // 计算分位数
        double p90 = calculatePercentile(drawCounts, 0.90);
        double p95 = calculatePercentile(drawCounts, 0.95);
        double p99 = calculatePercentile(drawCounts, 0.99);

        // 计算平均稀有度分布
        Map<Rarity, Double> avgRarityDist = new HashMap<>();
        for (Rarity rarity : Rarity.values()) {
            avgRarityDist.put(rarity, calculateAverage(rarityCounts.get(rarity)));
        }

        // 计算平均保底计数
        double avgPityCounter = calculateAverage(pityCounters);

        // 计算平均赠送次数
        double avgBonusCount = (double) totalBonusCount / totalSimulations;

        return SimulationResult.builder()
                .totalSimulations(totalSimulations)
                .targetSuccessCount(totalSimulations)
                .averageDrawsToTarget(avgDraws)
                .medianDrawsToTarget(medianDraws)
                .minDrawsToTarget(minDraws)
                .maxDrawsToTarget(maxDraws)
                .percentile90(p90)
                .percentile95(p95)
                .percentile99(p99)
                .averageRarityDistribution(avgRarityDist)
                .successRate(1.0)
                .expectedValue(avgDraws)
                .averagePityCounter(avgPityCounter)
                .averageBonusCount(avgBonusCount)
                .build();
    }

    private double calculateAverage(List<Integer> values) {
        if (values.isEmpty()) return 0;
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    private double calculateMedian(List<Integer> values) {
        if (values.isEmpty()) return 0;
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    private double calculatePercentile(List<Integer> values, double percentile) {
        if (values.isEmpty()) return 0;
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }
}
