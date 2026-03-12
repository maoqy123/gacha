package com.gacha.strategy;

import com.gacha.model.Card;
import com.gacha.model.Rarity;
import com.gacha.statistics.StatisticsTracker;

/**
 * 优化策略：抽到当期UP就收手，歪了则继续抽软保底
 * 
 * 规则：
 * 1. 如果抽到当期UP，停止
 * 2. 如果抽到6星但不是当期UP（歪了），检查是否还有足够资源抽一个软保底（65抽）
 *    - 有资源：继续抽
 *    - 没资源：停止
 * 3. 如果还没抽到6星，继续抽
 */
public class OneSixStarStrategy implements DrawStrategy {

    private static final int SOFT_PITY_THRESHOLD = 65;

    @Override
    public boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats) {
        return shouldContinue(lastDrawCard, context, stats, false);
    }

    @Override
    public boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats, boolean isCurrentUp) {
        // 检查资源是否充足
        if (!context.hasEnoughResources(1)) {
            return false;
        }

        // 首次抽卡，继续
        if (lastDrawCard == null) {
            return true;
        }

        // 如果抽到了6星
        if (lastDrawCard.getRarity() == Rarity.SIX_STAR) {
            // 如果是当期UP，停止
            if (isCurrentUp) {
                return false;
            }
            
            // 如果不是当期UP（歪了），检查是否还有足够资源抽一个软保底
            if (context.hasEnoughResources(SOFT_PITY_THRESHOLD)) {
                // 有资源，继续抽
                return true;
            }
            // 没资源，停止
            return false;
        }

        // 否则继续抽卡
        return true;
    }

    @Override
    public String getStrategyName() {
        return "抽到当期UP就收手";
    }

    @Override
    public String getStrategyDescription() {
        return "抽到当期UP即停止，歪了则继续抽软保底(65抽)";
    }
}
