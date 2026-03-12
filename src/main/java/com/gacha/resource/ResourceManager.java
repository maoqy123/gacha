package com.gacha.resource;

import lombok.Data;

/**
 * 资源管理器 - 管理抽卡资源
 */
@Data
public class ResourceManager {
    private int currentResources;  // 当前可用资源
    private int totalConsumed;     // 总消耗资源
    private int poolCount;         // 当前是第几个池子

    // 资源配置
    private static final int FIRST_POOL_RESOURCES = 120;   // 第1期：120石头
    private static final int SECOND_THIRD_RESOURCES = 80;  // 第2、3期：80石头
    private static final int LATER_POOL_RESOURCES = 60;    // 第4期及以后：60石头

    public ResourceManager() {
        this.poolCount = 0;
        this.totalConsumed = 0;
        this.currentResources = 0;
    }

    /**
     * 进入下一个池子，获得新资源
     */
    public void enterNextPool() {
        poolCount++;
        if (poolCount == 1) {
            currentResources = FIRST_POOL_RESOURCES;
        } else if (poolCount == 2 || poolCount == 3) {
            currentResources += SECOND_THIRD_RESOURCES;
        } else {
            currentResources += LATER_POOL_RESOURCES;
        }
    }

    /**
     * 尝试消耗资源
     * @param amount 需要消耗的资源数
     * @return 是否成功消耗
     */
    public boolean tryConsume(int amount) {
        if (currentResources >= amount) {
            currentResources -= amount;
            totalConsumed += amount;
            return true;
        }
        return false;
    }

    /**
     * 获取当前可用资源
     */
    public int getAvailableResources() {
        return currentResources;
    }

    /**
     * 检查是否有足够资源
     */
    public boolean hasEnoughResources(int amount) {
        return currentResources >= amount;
    }

    /**
     * 获取当前池子编号
     */
    public int getCurrentPoolNumber() {
        return poolCount;
    }

    /**
     * 获取总消耗资源
     */
    public int getTotalConsumed() {
        return totalConsumed;
    }

    /**
     * 获取本池子获得的资源
     */
    public int getCurrentPoolResources() {
        if (poolCount == 1) {
            return FIRST_POOL_RESOURCES;
        } else if (poolCount == 2 || poolCount == 3) {
            return SECOND_THIRD_RESOURCES;
        } else if (poolCount > 3) {
            return LATER_POOL_RESOURCES;
        }
        return 0;
    }
}
