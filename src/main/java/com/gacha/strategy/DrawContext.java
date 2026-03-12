package com.gacha.strategy;

import com.gacha.resource.ResourceManager;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽卡上下文 - 只记录抽卡过程中的状态
 * 统计功能已移至 StatisticsTracker
 */
@Data
public class DrawContext {
    private int pityCounter;        // 软保底计数器（跨池继承，出任何6星重置）
    private int poolPityCounter;    // 硬保底计数器（每个池子独立，出当期UP重置）
    private int totalDraws;         // 总抽卡次数（含赠送）
    private int paidDraws;          // 实际付费抽卡次数
    private boolean usedFreeTenPull;
    private boolean used30PullBonus;
    private boolean used60PullBonus;
    private boolean hasNextPoolFreeTenPull;
    private List<String> bonusRecords;
    private ResourceManager resourceManager;
    private int initialPoolResources;

    public DrawContext() {
        this.pityCounter = 0;
        this.poolPityCounter = 0;
        this.totalDraws = 0;
        this.paidDraws = 0;
        this.usedFreeTenPull = false;
        this.used30PullBonus = false;
        this.used60PullBonus = false;
        this.hasNextPoolFreeTenPull = false;
        this.bonusRecords = new ArrayList<>();
        this.resourceManager = new ResourceManager();
        this.initialPoolResources = 0;
    }

    public DrawContext(ResourceManager resourceManager) {
        this();
        this.resourceManager = resourceManager;
    }

    public void incrementPity() {
        pityCounter++;
        poolPityCounter++;
    }

    /**
     * 重置软保底计数器（出任何6星时调用）
     */
    public void resetSoftPity() {
        pityCounter = 0;
    }

    /**
     * 重置硬保底计数器（出当期UP时调用）
     */
    public void resetHardPity() {
        poolPityCounter = 0;
    }

    /**
     * 重置所有保底计数器（硬保底触发时调用）
     */
    public void resetAllPity() {
        pityCounter = 0;
        poolPityCounter = 0;
    }

    public void incrementTotalDraws(int count) {
        totalDraws += count;
    }

    public void incrementPaidDraws(int count) {
        paidDraws += count;
    }

    public void addBonusRecord(String record) {
        bonusRecords.add(record);
    }

    /**
     * 检查资源是否充足
     */
    public boolean hasEnoughResources(int amount) {
        return resourceManager != null && resourceManager.hasEnoughResources(amount);
    }

    /**
     * 尝试消耗资源
     */
    public boolean tryConsumeResources(int amount) {
        if (resourceManager != null) {
            return resourceManager.tryConsume(amount);
        }
        return false;
    }

    /**
     * 获取当前可用资源
     */
    public int getAvailableResources() {
        return resourceManager != null ? resourceManager.getAvailableResources() : 0;
    }

    /**
     * 获取进入当前池子时的初始资源
     */
    public int getInitialPoolResources() {
        return initialPoolResources;
    }

    /**
     * 进入下一个池子
     */
    public void enterNextPool() {
        if (resourceManager != null) {
            resourceManager.enterNextPool();
            this.initialPoolResources = resourceManager.getAvailableResources();
        }
        // 重置池子相关状态
        usedFreeTenPull = false;
        used30PullBonus = false;
        used60PullBonus = false;
        paidDraws = 0;
        // 硬保底计数器每个池子独立，需要重置
        poolPityCounter = 0;
        // 软保底计数器跨池继承，不重置
    }
}
