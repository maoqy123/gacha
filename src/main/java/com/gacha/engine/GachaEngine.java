package com.gacha.engine;

import com.gacha.model.Card;
import com.gacha.model.CardPool;
import com.gacha.model.CardPoolRule;
import com.gacha.model.Rarity;
import com.gacha.strategy.DrawContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 抽卡引擎 - 负责处理抽卡规则和概率计算
 * 与策略分离，策略只负责决定是否继续抽卡
 */
public class GachaEngine {

    private final Random random;
    private final CardPool pool;
    private final CardPoolRule rule;

    public GachaEngine(CardPool pool) {
        this.random = new Random();
        this.pool = pool;
        this.rule = pool.getRule();
    }

    /**
     * 执行单次抽卡
     * @param context 抽卡上下文
     * @return 抽到的卡片
     */
    public Card draw(DrawContext context) {
        // 检查并应用赠送的抽卡
        checkAndApplyBonus(context);

        // 执行单次抽卡
        return singleDraw(context);
    }

    /**
     * 执行多次抽卡（十连）
     * @param context 抽卡上下文
     * @param count 抽卡次数
     * @return 抽到的卡片列表
     */
    public List<Card> drawMultiple(DrawContext context, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(draw(context));
        }
        return cards;
    }

    /**
     * 单次抽卡逻辑
     */
    private Card singleDraw(DrawContext context) {
        // 先增加保底计数器
        context.incrementPity();

        // 检查是否触发硬保底（每个池子独立）
        int poolPityCounter = context.getPoolPityCounter();
        boolean isHardPity = rule.isHardPityTriggered(poolPityCounter);

        // 硬保底必出当期UP
        if (isHardPity && rule.isHardPityGuaranteedCurrentUp()) {
            List<Card> currentUps = pool.getCurrentUpCharacters();
            Card card = currentUps.get(random.nextInt(currentUps.size()));
            context.resetAllPity();
            return card;
        }

        // 使用软保底计数器计算6星概率（跨池继承）
        int pityCounter = context.getPityCounter();
        double sixStarRate = rule.calculateSixStarRate(pityCounter);
        double roll = random.nextDouble();

        if (roll < sixStarRate) {
            return drawSixStar(context);
        } else {
            return drawNonSixStar();
        }
    }

    /**
     * 抽取6星卡（非硬保底情况）
     */
    private Card drawSixStar(DrawContext context) {
        // 50%概率当期UP
        if (random.nextDouble() < rule.getCurrentUpRate()) {
            List<Card> currentUps = pool.getCurrentUpCharacters();
            Card card = currentUps.get(random.nextInt(currentUps.size()));
            // 出当期UP，重置所有保底计数器
            context.resetAllPity();
            return card;
        } else {
            // 50%概率非当期UP（歪了）
            List<Card> nonCurrentUps = pool.getNonCurrentUpSixStars();
            Card card = nonCurrentUps.get(random.nextInt(nonCurrentUps.size()));
            // 歪了只重置软保底计数器，硬保底计数器继续累积
            context.resetSoftPity();
            return card;
        }
    }

    /**
     * 抽取非6星卡
     */
    private Card drawNonSixStar() {
        return Card.builder()
                .id("low_star")
                .name("低星卡")
                .rarity(Rarity.SR)
                .isLimited(false)
                .build();
    }

    /**
     * 检查并应用赠送的抽卡
     */
    private void checkAndApplyBonus(DrawContext context) {
        int totalDraws = context.getTotalDraws();

        // 第一次十连免费
        if (!context.isUsedFreeTenPull() && totalDraws == 0) {
            context.setUsedFreeTenPull(true);
            context.addBonusRecord("第一次十连免费");
        }

        // 30抽赠送十连
        if (!context.isUsed30PullBonus() && totalDraws >= rule.getFreeTenPullAt30()) {
            context.setUsed30PullBonus(true);
            context.addBonusRecord("30抽赠送十连");
        }

        // 60抽赠送下次卡池十连
        if (!context.isUsed60PullBonus() && totalDraws >= rule.getFreeNextPoolTenPullAt60()) {
            context.setUsed60PullBonus(true);
            context.setHasNextPoolFreeTenPull(true);
            context.addBonusRecord("60抽赠送下次卡池十连");
        }
    }

    /**
     * 检查是否是当期UP
     */
    public boolean isCurrentUp(Card card) {
        for (Card upCard : pool.getCurrentUpCharacters()) {
            if (upCard.getId().equals(card.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否是UP角色（任何期的UP）
     */
    public boolean isUpCharacter(Card card) {
        Set<String> allUpIds = getAllUpIds();
        return allUpIds.contains(card.getId());
    }

    /**
     * 获取所有UP角色的ID集合
     */
    private Set<String> getAllUpIds() {
        Set<String> allUpIds = new HashSet<>();
        for (List<Card> ups : pool.getUpHistory().values()) {
            for (Card up : ups) {
                allUpIds.add(up.getId());
            }
        }
        return allUpIds;
    }
}
