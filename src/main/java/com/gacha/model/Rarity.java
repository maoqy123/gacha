package com.gacha.model;

/**
 * 卡牌稀有度枚举
 */
public enum Rarity {
    SIX_STAR(6, "6星", 0.008),    // 6星，基础概率0.8%
    SSR(5, "SSR", 0.01),          // 超稀有，基础概率1%
    SR(4, "SR", 0.10),            // 稀有，基础概率10%
    R(3, "R", 0.30),              // 普通，基础概率30%
    N(2, "N", 0.59);              // 常见，基础概率59%

    private final int level;
    private final String displayName;
    private final double baseProbability;

    Rarity(int level, String displayName, double baseProbability) {
        this.level = level;
        this.displayName = displayName;
        this.baseProbability = baseProbability;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseProbability() {
        return baseProbability;
    }
}
