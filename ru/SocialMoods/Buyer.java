package ru.SocialMoods;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.SocialMoods.autobuyer.AutoBuyerAPI;
import ru.SocialMoods.autobuyer.AutoBuyerListener;
import ru.SocialMoods.command.BuyerCommand;
import ru.SocialMoods.model.ShopItem;
import ru.SocialMoods.util.TimerTask;

public class Buyer extends PluginBase {

    private final Map<Integer, ShopItem> shopItems = new HashMap<>();
    private Config config;
    private ScheduledExecutorService scheduler;
    private AutoBuyerAPI autoBuyerAPI;

    public Buyer() {
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);
        autoBuyerAPI = new AutoBuyerAPI();
        loadShopItems();
        getServer().getPluginManager().registerEvents(new AutoBuyerListener(autoBuyerAPI, this), this);
        this.getServer().getCommandMap().register("buyer", new BuyerCommand(this));
        startTimerTask();
    }

    private void startTimerTask() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new TimerTask(this), 0, getConfig().getInt("update-period"), TimeUnit.MINUTES);
    }

    public Map<Integer, ShopItem> getShopItems() {
        return shopItems;
    }

    public void loadShopItems() {
        shopItems.clear();
        for (Object keyObj : config.getKeys(false)) {
            String key = String.valueOf(keyObj);
            int itemId = config.getInt(key + ".itemId");
            int count = config.getInt(key + ".count");
            int price = config.getInt(key + ".price");
            String url = config.getString(key + ".url");


            int slot;
            try {
                slot = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                continue;
            }

            ShopItem shopItem = new ShopItem(itemId, count, price, url);
            shopItems.put(slot, shopItem);
        }
    }

    public AutoBuyerAPI getAutoBuyerAPI() {
        return autoBuyerAPI;
    }

    @Override
    public void onDisable() {
        scheduler.shutdown();
    }
}