package ru.SocialMoods.util;

import ru.SocialMoods.Buyer;
import ru.SocialMoods.model.ShopItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PriceCalculator {

    private final Buyer plugin;
    private final double baseVariation;
    private final Map<Integer, Integer> salesCountMap = new ConcurrentHashMap<>();

    public PriceCalculator(Buyer plugin) {
        this.plugin = plugin;
        this.baseVariation = plugin.getConfig().getDouble("price-ratio");
        for (int itemId : plugin.getShopItems().keySet()) {
            salesCountMap.put(itemId, 0);
        }
    }

    public int getAdjustedPrice(ShopItem shopItem) {
        int initialPrice = shopItem.getPrice();
        int adjustedPrice = (int) (initialPrice);

        int salesCount = salesCountMap.getOrDefault(shopItem.getItem().getId(), 0);

        if (salesCount > 100) {
            adjustedPrice = (int) (adjustedPrice * (1 - baseVariation));
        } else if (salesCount < 10) {
            adjustedPrice = (int) (adjustedPrice * (1 + baseVariation));
        }

        return adjustedPrice;
    }

    public void incrementSalesCount(int itemId) {
        salesCountMap.put(itemId, salesCountMap.getOrDefault(itemId, 0) + 1);
    }

    public void resetSalesCount() {
        salesCountMap.replaceAll((k, v) -> 0);
    }
}