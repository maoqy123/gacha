package com.gacha.strategy;

import com.gacha.model.Card;
import com.gacha.statistics.StatisticsTracker;

/**
 * 抽卡策略接口 - 只负责判断是否继续抽卡
 * 抽卡逻辑由 GachaEngine 处理
 * 统计逻辑由 StatisticsTracker 处理
 */
public interface DrawStrategy {

    /**
     * 判断是否继续抽卡
     * @param lastDrawCard 上一次抽到的卡片（首次为null）
     * @param context 抽卡上下文
     * @param stats 统计追踪器
     * @return true表示继续抽卡，false表示停止
     */
    boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats);

    /**
     * 判断是否继续抽卡（带当期UP判断）
     * @param lastDrawCard 上一次抽到的卡片（首次为null）
     * @param context 抽卡上下文
     * @param stats 统计追踪器
     * @param isCurrentUp 上一次抽到的卡片是否是当期UP
     * @return true表示继续抽卡，false表示停止
     */
    default boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats, boolean isCurrentUp) {
        return shouldContinue(lastDrawCard, context, stats);
    }

    /**
     * 获取策略名称
     */
    String getStrategyName();

    /**
     * 获取策略描述
     */
    String getStrategyDescription();
}
