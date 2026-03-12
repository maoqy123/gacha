package com.gacha.strategy;

import com.gacha.model.Card;
import com.gacha.statistics.StatisticsTracker;

/**
 * 120+30循环策略（基于进入池子时的初始判断）
 *
 * 执行逻辑：
 * - 进入池子时计算理论可用抽卡次数（计入保底的）：
 *   - 初始资源（付费抽卡）
 *   - + 上期60抽赠送的10抽（如果有，计入保底）
 *   - + 本期第一次十连免费（计入保底）
 * - 如果理论可用 ≥ 120抽：本期目标120抽拿UP
 * - 如果理论可用 < 120抽：本期目标30抽拿赠送
 * - 无论哪种情况，出UP即停
 *
 * 关键点：目标在进入池子时就确定，不随抽卡过程改变
 */
public class LoopStrategy implements DrawStrategy {

    private static final int TARGET_120 = 120;  // 资源充足时的目标：拿UP
    private static final int TARGET_30 = 30;    // 资源不足时的目标：拿赠送
    private static final int TARGET_THRESHOLD = 120;  // 目标阈值
    private static final int BONUS_FROM_LAST_POOL = 10;  // 上期赠送的10抽（计入保底）
    private static final int BONUS_FIRST_TEN_FREE = 10;  // 本期第一次十连免费（计入保底）

    @Override
    public boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats) {
        // 检查资源是否充足（还能不能抽）
        if (!context.hasEnoughResources(1)) {
            return false;
        }

        int paidDraws = context.getPaidDraws();
        int initialResources = context.getInitialPoolResources();
        boolean hasNextPoolFreeTenPull = context.isHasNextPoolFreeTenPull(); // 是否有上期赠送的10抽

        // 计算进入池子时的理论可用抽卡次数（计入保底的）
        // 理论可用 = 初始资源 + 上期赠送（如果有）+ 本期第一次十连免费
        int theoreticalAvailable = initialResources + BONUS_FIRST_TEN_FREE;
        if (hasNextPoolFreeTenPull) {
            theoreticalAvailable += BONUS_FROM_LAST_POOL;
        }

        // 如果已经拿到了当期UP，停止
        if (stats.getCurrentPoolCurrentUpCount() > 0) {
            return false;
        }

        // 根据进入池子时的理论可用次数决定本期目标
        // 理论可用 ≥ 120：本期目标120抽
        // 理论可用 < 120：本期目标30抽
        int targetDraws = (theoreticalAvailable >= TARGET_THRESHOLD) ? TARGET_120 : TARGET_30;

        // 检查是否达到目标抽数
        return paidDraws < targetDraws;
    }

    @Override
    public String getStrategyName() {
        return "120+30循环策略";
    }

    @Override
    public String getStrategyDescription() {
        return "进入池子时判断：理论可用≥120则目标120抽，<120则目标30抽，出UP即停";
    }
}
