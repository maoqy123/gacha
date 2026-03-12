package com.gacha.strategy;

import com.gacha.model.Card;
import com.gacha.statistics.StatisticsTracker;

/**
 * 投入120抽保底策略
 * 确保拿到当期UP，120抽硬保底
 * 如果提前出当期UP也停止
 * 资源不足时会提前退出
 */
public class HardPityStrategy implements DrawStrategy {

    private final int maxDraws;

    public HardPityStrategy() {
        this.maxDraws = 120;
    }

    @Override
    public boolean shouldContinue(Card lastDrawCard, DrawContext context, StatisticsTracker stats) {
        // 检查资源是否充足
        if (!context.hasEnoughResources(1)) {
            return false;
        }

        // 检查是否达到最大抽卡次数
        if (context.getPaidDraws() >= maxDraws) {
            return false;
        }

        // 首次抽卡，继续
        if (lastDrawCard == null) {
            return true;
        }

        // 如果已经获得了当期UP，停止
        if (stats.getCurrentPoolCurrentUpCount() > 0) {
            return false;
        }

        // 否则继续抽卡
        return true;
    }

    @Override
    public String getStrategyName() {
        return "120抽硬保底策略";
    }

    @Override
    public String getStrategyDescription() {
        return "投入最多120抽确保拿到当期UP，提前出也停止，资源不足会提前退出";
    }
}
