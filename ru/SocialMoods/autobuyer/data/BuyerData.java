package ru.SocialMoods.autobuyer.data;

import cn.nukkit.item.Item;

public record BuyerData(Item item, int quantity, double pricePerItem) {

    @Override
    public int quantity() {
        return quantity + 1;
    }
}