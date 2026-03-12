package com.gacha.statistics;

import com.gacha.model.Card;
import com.gacha.model.Rarity;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 统计追踪器 - 负责记录抽卡统计信息
 * 与策略分离，独立管理统计数据
 */
@Data
public class StatisticsTracker {

    // 记录已获得的所有UP角色（用于判断重复）
    private Set<String> obtainedUpCharacters;

    // 当前池子统计
    private int currentPoolSixStars;
    private int currentPoolNewUp;
    private int currentPoolDuplicateUp;

    // 总体统计
    private int totalSixStars;
    private int totalNewUp;
    private int totalDuplicateUp;

    // 稀有度分布
    private Map<Rarity, Integer> rarityDistribution;

    public StatisticsTracker() {
        this.obtainedUpCharacters = new HashSet<>();
        this.rarityDistribution = new HashMap<>();
        resetCurrentPoolStats();
    }

    /**
     * 记录抽到的卡片
     * @param card 抽到的卡片
     * @param isUpCharacter 是否是UP角色（任何期的UP）
     * @return 如果是新UP返回true，否则返回false
     */
    public boolean recordCard(Card card, boolean isUpCharacter) {
        // 记录稀有度
        rarityDistribution.merge(card.getRarity(), 1, Integer::sum);

        // 如果是6星
        if (card.getRarity() == Rarity.SIX_STAR) {
            currentPoolSixStars++;
            totalSixStars++;

            // 如果是UP角色（任何期的UP）
            if (isUpCharacter) {
                // 检查是否是新UP
                if (!obtainedUpCharacters.contains(card.getId())) {
                    obtainedUpCharacters.add(card.getId());
                    currentPoolNewUp++;
                    totalNewUp++;
                    return true; // 新UP（有效UP）
                } else {
                    currentPoolDuplicateUp++;
                    totalDuplicateUp++;
                    return false; // 重复UP
                }
            }
        }
        return false;
    }

    /**
     * 进入下一个池子，重置当前池子统计
     */
    public void enterNextPool() {
        resetCurrentPoolStats();
    }

    /**
     * 重置当前池子统计
     */
    private void resetCurrentPoolStats() {
        currentPoolSixStars = 0;
        currentPoolNewUp = 0;
        currentPoolDuplicateUp = 0;
    }

    /**
     * 获取当前池子获得的6星数量
     */
    public int getCurrentPoolSixStarCount() {
        return currentPoolSixStars;
    }

    /**
     * 获取当前池子获得的新UP数量
     */
    public int getCurrentPoolNewUpCount() {
        return currentPoolNewUp;
    }

    /**
     * 获取当前池子获得的重复UP数量
     */
    public int getCurrentPoolDuplicateUpCount() {
        return currentPoolDuplicateUp;
    }

    /**
     * 获取当前池子获得的当期UP总数
     */
    public int getCurrentPoolCurrentUpCount() {
        return currentPoolNewUp + currentPoolDuplicateUp;
    }

    /**
     * 获取不同UP角色的数量
     */
    public int getUniqueUpCount() {
        return obtainedUpCharacters.size();
    }

    /**
     * 获取总6星数量
     */
    public int getTotalSixStars() {
        return totalSixStars;
    }

    /**
     * 获取总新UP数量
     */
    public int getTotalNewUp() {
        return totalNewUp;
    }

    /**
     * 获取总重复UP数量
     */
    public int getTotalDuplicateUp() {
        return totalDuplicateUp;
    }

    /**
     * 获取总当期UP数量
     */
    public int getTotalCurrentUp() {
        return totalNewUp + totalDuplicateUp;
    }

    /**
     * 获取稀有度分布
     */
    public Map<Rarity, Integer> getRarityDistribution() {
        return new HashMap<>(rarityDistribution);
    }
}
