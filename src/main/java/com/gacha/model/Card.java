package com.gacha.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡牌实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private String id;
    private String name;
    private Rarity rarity;
    private boolean isLimited;  // 是否为限定卡
    private String description;

    @Override
    public String toString() {
        return String.format("%s[%s]", name, rarity.getDisplayName());
    }
}
