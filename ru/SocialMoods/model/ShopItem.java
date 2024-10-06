package ru.SocialMoods.model;

import cn.nukkit.item.Item;

public class ShopItem {

    private final int itemId;
    private final int count;
    private final int price;
    private final String imageUrl;

    public ShopItem(int itemId, int count, int price, String imageUrl) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Item getItem() {
        return Item.get(itemId, 0, count);
    }

    public int getPrice() {
        return price;
    }

    public int getCount() {
        return count;
    }

    public String getImageUrl() {return imageUrl;}

    public String getItemName() {
        return Item.get(this.itemId).getName();
    }
}