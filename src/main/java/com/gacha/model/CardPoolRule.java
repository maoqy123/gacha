package com.gacha.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 卡池规则类 - 管理游戏本身的抽卡规则
 */
@Data
@Builder
public class CardPoolRule {

    // 基础概率配置
    @Builder.Default
    private double sixStarBaseRate = 0.008;  // 6星基础概率 0.8%

    @Builder.Default
    private double currentUpRate = 0.5;  // 出6星时，当期UP的概率 50%

    @Builder.Default
    private int softPityThreshold = 65;  // 软保底阈值

    @Builder.Default
    private double softPityIncrement = 0.05;  // 软保底每抽增加5%

    @Builder.Default
    private int hardPityThreshold = 120;  // 硬保底阈值

    // 保底规则
    @Builder.Default
    private boolean hardPityGuaranteedCurrentUp = true;  // 硬保底必出当期UP

    // 赠送规则
    @Builder.Default
    private int freeTenPullAt30 = 30;  // 第一次30抽送十连

    @Builder.Default
    private int freeNextPoolTenPullAt60 = 60;  // 第一次60抽送下次卡池十连

    @Builder.Default
    private boolean firstTenPullFree = true;  // 每个卡池第一次十连免费

    // 常驻角色数量
    @Builder.Default
    private int permanentCharacterCount = 5;  // 常驻角色数量

    /**
     * 计算当前6星概率
     * @param currentPity 当前保底计数
     * @return 6星概率
     */
    public double calculateSixStarRate(int currentPity) {
        if (currentPity >= hardPityThreshold - 1) {
            return 1.0;  // 硬保底前必出
        }

        double rate = sixStarBaseRate;

        // 软保底机制
        if (currentPity >= softPityThreshold) {
            int overflow = currentPity - softPityThreshold + 1;
            rate += overflow * softPityIncrement;
        }

        return Math.min(rate, 1.0);
    }

    /**
     * 判断是否应该触发硬保底
     * @param currentPity 当前保底计数（已经增加后的值）
     * @return 是否触发硬保底
     */
    public boolean isHardPityTriggered(int currentPity) {
        // currentPity是增加后的值，第120抽时currentPity=120
        return currentPity >= hardPityThreshold;
    }

    /**
     * 判断是否应该触发软保底（概率递增）
     * @param currentPity 当前保底计数
     * @return 是否在软保底区间
     */
    public boolean isInSoftPityRange(int currentPity) {
        return currentPity >= softPityThreshold && currentPity < hardPityThreshold;
    }

    /**
     * 获取规则描述
     */
    public String getRuleDescription() {
        return String.format(
                "6星基础概率: %.1f%% | 当期UP概率: %.0f%% | 软保底: %d抽(+%.0f%%/抽) | 硬保底: %d抽必出UP",
                sixStarBaseRate * 100,
                currentUpRate * 100,
                softPityThreshold,
                softPityIncrement * 100,
                hardPityThreshold
        );
    }
}
