package ru.SocialMoods.util;

import ru.SocialMoods.Buyer;

public class TimerTask implements Runnable {

    private final Buyer plugin;

    public TimerTask(Buyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.loadShopItems();
        PriceCalculator priceCalculator = new PriceCalculator(plugin);
        priceCalculator.resetSalesCount();
        plugin.getServer().broadcastMessage(plugin.getConfig().getString("update-message"));
    }
}