package com.gacha;

import com.gacha.model.Card;
import com.gacha.model.CardPool;
import com.gacha.model.Rarity;
import com.gacha.simulator.GachaSimulator;
import com.gacha.statistics.StatisticsTracker;
import com.gacha.strategy.*;

import java.util.*;

/**
 * 抽卡逻辑验证测试 - 详细输出每个卡池的抽卡结果
 */
public class GachaVerificationTest {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       抽卡逻辑验证测试                       ");
        System.out.println("==============================================\n");

        CardPool pool = CardPool.createDefaultPool();

        // 测试所有策略
        testStrategy(pool, new OneSixStarStrategy(), "抽到一个6星就收手");
        testStrategy(pool, new HardPityStrategy(), "120抽硬保底");
        testStrategy(pool, new LoopStrategy(), "120+30循环");
    }

    private static void testStrategy(CardPool pool, DrawStrategy strategy, String strategyName) {
        System.out.println("\n\n##############################################");
        System.out.println("策略: " + strategyName);
        System.out.println("##############################################\n");

        GachaSimulator simulator = new GachaSimulator(pool, strategy);
        DrawContext context = new DrawContext();
        StatisticsTracker stats = new StatisticsTracker();

        int poolCount = 20;

        // 用于追踪已获得的所有UP角色（所有期的UP都记录）
        Set<String> obtainedUpIds = new HashSet<>();

        // 执行多池模拟
        GachaSimulator.MultiPoolSimulationResult result = simulator.simulateMultiplePools(poolCount, context, stats);

        // 输出每个卡池的详细结果
        List<GachaSimulator.SingleSimulationResult> poolResults = result.getPoolResults();
        
        int previousResources = 120; // 初始资源
        
        for (int i = 0; i < poolResults.size(); i++) {
            GachaSimulator.SingleSimulationResult poolResult = poolResults.get(i);
            
            // 计算本池消耗的资源
            int currentResources = poolResult.getAvailableResources();
            int consumedThisPool = previousResources + 80 - currentResources;
            if (i == 0) {
                consumedThisPool = 120 - currentResources;
            }

            // 更新卡池期数以获取正确的当期UP
            int period = i + 1;
            pool.setCurrentPeriod(period);
            
            System.out.println("══════════════════════════════════════════════");
            System.out.println("【卡池 " + period + "】");
            System.out.println("══════════════════════════════════════════════");
            System.out.println("投入石头: " + consumedThisPool + " 石");
            System.out.println("投入抽数: " + poolResult.getPaidDraws() + " 抽");
            System.out.println("剩余石头: " + currentResources + " 石");
            
            // 获取当期UP角色ID
            List<Card> currentUps = pool.getCurrentUpCharacters();
            Set<String> currentUpIds = new HashSet<>();
            for (Card up : currentUps) {
                currentUpIds.add(up.getId());
            }
            
            // 获取所有UP角色ID（用于判断是否是UP角色）
            Set<String> allUpIds = getAllUpIds(pool);
            
            // 输出6星详情
            List<Card> cards = poolResult.getCards();
            List<String> sixStarResults = new ArrayList<>();
            int newUpCount = 0;
            int dupUpCount = 0;

            for (Card card : cards) {
                if (card.getRarity() == Rarity.SIX_STAR) {
                    String status;
                    boolean isCurrentUp = currentUpIds.contains(card.getId());
                    boolean isUpCharacter = allUpIds.contains(card.getId());
                    boolean isObtained = obtainedUpIds.contains(card.getId());
                    
                    if (isUpCharacter) {
                        // 是UP角色（任何期的UP）
                        if (isObtained) {
                            status = "【重复UP】";
                            dupUpCount++;
                        } else {
                            // 首次获得的UP角色都是有效UP
                            status = "【有效UP】";
                            obtainedUpIds.add(card.getId());
                            newUpCount++;
                        }
                    } else {
                        status = "【歪了】";
                    }
                    
                    sixStarResults.add(card.getName() + " " + status);
                }
            }

            // 输出6星结果
            System.out.println("\n6星结果:");
            if (sixStarResults.isEmpty()) {
                System.out.println("  无6星");
            } else {
                for (int j = 0; j < sixStarResults.size(); j++) {
                    System.out.println("  " + (j + 1) + ". " + sixStarResults.get(j));
                }
            }
            
            // 输出本池小结
            System.out.println("\n本池小结:");
            System.out.println("  6星数量: " + poolResult.getSixStarCount());
            System.out.println("  有效UP: " + newUpCount + " | 重复UP: " + dupUpCount);
            
            previousResources = currentResources;
        }

        // 输出总体统计
        System.out.println("\n\n##############################################");
        System.out.println("【总体统计】");
        System.out.println("##############################################");
        System.out.println("总卡池数: " + poolCount);
        System.out.println("总消耗石头: " + result.getTotalConsumedResources() + " 石");
        System.out.println("剩余石头: " + result.getFinalResources() + " 石");
        System.out.println();
        System.out.println("总6星数: " + result.getTotalSixStars());
        System.out.println("总当期UP: " + result.getTotalCurrentUp());
        System.out.println("  - 有效新UP: " + result.getTotalNewUp());
        System.out.println("  - 重复UP: " + result.getTotalDuplicateUp());
        System.out.println("不同UP角色: " + result.getUniqueUpCount() + " 个");
        System.out.println();
        System.out.printf("平均每池消耗: %.1f 石%n", (double) result.getTotalConsumedResources() / poolCount);
        System.out.printf("平均每池6星: %.2f 个%n", (double) result.getTotalSixStars() / poolCount);
        System.out.printf("平均每池有效UP: %.2f 个%n", (double) result.getTotalNewUp() / poolCount);
    }

    /**
     * 获取所有UP角色的ID集合
     */
    private static Set<String> getAllUpIds(CardPool pool) {
        Set<String> allUpIds = new HashSet<>();
        Map<Integer, List<Card>> upHistory = pool.getUpHistory();
        for (List<Card> ups : upHistory.values()) {
            for (Card up : ups) {
                allUpIds.add(up.getId());
            }
        }
        return allUpIds;
    }
}
