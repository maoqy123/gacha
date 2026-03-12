package com.gacha.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 卡池配置 - 支持动态期数
 */
@Data
@Builder
public class CardPool {
    private String poolId;
    private String poolName;
    private CardPoolRule rule;

    // 所有期的UP角色历史 (期数 -> UP角色列表)
    @Builder.Default
    private Map<Integer, List<Card>> upHistory = new HashMap<>();

    // 常驻角色
    @Builder.Default
    private List<Card> permanentCharacters = new ArrayList<>();

    // 当前期数（运行时设置）
    private int currentPeriod;

    /**
     * 设置当前期数并更新UP角色
     */
    public void setCurrentPeriod(int period) {
        this.currentPeriod = period;
    }

    /**
     * 获取当期UP角色
     */
    public List<Card> getCurrentUpCharacters() {
        return upHistory.getOrDefault(currentPeriod, new ArrayList<>());
    }

    /**
     * 获取上一期UP角色
     */
    public List<Card> getPreviousUpCharacters() {
        return upHistory.getOrDefault(currentPeriod - 1, new ArrayList<>());
    }

    /**
     * 获取上上期UP角色
     */
    public List<Card> getPreviousPreviousUpCharacters() {
        return upHistory.getOrDefault(currentPeriod - 2, new ArrayList<>());
    }

    /**
     * 获取非当期UP的6星角色（用于50%概率池）
     * 包含：常驻 + 上一期UP + 上上期UP
     * 
     * 特殊规则：
     * - 第1期：没有上一期和上上期，使用2、3期作为替代
     * - 第2期：没有上上期，使用1、3期作为替代
     * - 第3期及以后：正常使用上一期和上上期
     */
    public List<Card> getNonCurrentUpSixStars() {
        List<Card> pool = new ArrayList<>();
        pool.addAll(permanentCharacters);

        if (currentPeriod == 1) {
            // 第1期：使用2、3期作为替代
            pool.addAll(upHistory.getOrDefault(2, new ArrayList<>()));
            pool.addAll(upHistory.getOrDefault(3, new ArrayList<>()));
        } else if (currentPeriod == 2) {
            // 第2期：使用1、3期作为替代
            pool.addAll(upHistory.getOrDefault(1, new ArrayList<>()));
            pool.addAll(upHistory.getOrDefault(3, new ArrayList<>()));
        } else {
            // 第3期及以后：正常使用上一期和上上期
            pool.addAll(upHistory.getOrDefault(currentPeriod - 1, new ArrayList<>()));
            pool.addAll(upHistory.getOrDefault(currentPeriod - 2, new ArrayList<>()));
        }

        return pool;
    }

    /**
     * 获取所有6星角色池
     */
    public List<Card> getSixStarPool() {
        List<Card> pool = new ArrayList<>();
        pool.addAll(getCurrentUpCharacters());
        pool.addAll(getNonCurrentUpSixStars());
        return pool;
    }

    /**
     * 创建默认卡池（包含20期的UP角色）
     */
    public static CardPool createDefaultPool() {
        CardPoolRule rule = CardPoolRule.builder().build();
        Map<Integer, List<Card>> upHistory = new HashMap<>();

        // 生成20期的UP角色（每期1个）
        for (int period = 1; period <= 20; period++) {
            String name = "第" + period + "期UP";
            upHistory.put(period, createUpCharacters(name));
        }

        return CardPool.builder()
                .poolId("default_pool")
                .poolName("默认卡池")
                .rule(rule)
                .upHistory(upHistory)
                .permanentCharacters(createPermanentCharacters())
                .currentPeriod(1)
                .build();
    }

    private static List<Card> createUpCharacters(String... names) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            cards.add(Card.builder()
                    .id(names[i])
                    .name(names[i])
                    .rarity(Rarity.SIX_STAR)
                    .isLimited(true)
                    .build());
        }
        return cards;
    }

    private static List<Card> createPermanentCharacters() {
        List<Card> cards = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            cards.add(Card.builder()
                    .id("常驻6星" + i)
                    .name("常驻6星" + i)
                    .rarity(Rarity.SIX_STAR)
                    .isLimited(false)
                    .build());
        }
        return cards;
    }
}
