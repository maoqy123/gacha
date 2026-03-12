package com.gacha.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单次抽卡结果
 */
@Data
@Builder
public class DrawResult {
    private List<Card> cards;
    private int totalDraws;           // 总抽卡次数
    private int pityCounter;          // 6星保底计数器
    private boolean isPityTriggered;  // 是否触发保底

    // 赠送记录
    @Builder.Default
    private List<String> bonusRecords = new ArrayList<>();

    // 是否使用了免费十连
    private boolean usedFreeTenPull;

    // 是否使用了30抽赠送十连
    private boolean used30PullBonus;

    // 是否使用了60抽赠送下次十连
    private boolean used60PullBonus;

    // 新UP数量（首次获得的不同UP角色）
    @Builder.Default
    private int newUpCount = 0;

    // 重复UP数量（已经获得过的UP角色）
    @Builder.Default
    private int duplicateUpCount = 0;

    public int getSixStarCount() {
        return (int) cards.stream()
                .filter(card -> card.getRarity() == Rarity.SIX_STAR)
                .count();
    }

    public int getSSRCount() {
        return (int) cards.stream()
                .filter(card -> card.getRarity() == Rarity.SSR)
                .count();
    }

    public int getSRCount() {
        return (int) cards.stream()
                .filter(card -> card.getRarity() == Rarity.SR)
                .count();
    }

    /**
     * 获取当期UP数量（包含新UP和重复UP）
     */
    public int getCurrentUpCount() {
        return newUpCount + duplicateUpCount;
    }

    /**
     * 获取有效UP数量（新UP）
     */
    public int getEffectiveUpCount() {
        return newUpCount;
    }

    /**
     * 获取无效UP数量（重复UP）
     */
    public int getWastedUpCount() {
        return duplicateUpCount;
    }
}
