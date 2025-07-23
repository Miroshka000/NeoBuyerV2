package ru.SocialMoods;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
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
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                saveResource("config.yml", false);
            }
            
            config = new Config(configFile, Config.YAML);
            
            autoBuyerAPI = new AutoBuyerAPI(this);
            
            loadShopItems();
            
            getServer().getPluginManager().registerEvents(new AutoBuyerListener(autoBuyerAPI, this), this);
            this.getServer().getCommandMap().register("buyer", new BuyerCommand(this));
            
            startTimerTask();
            
            this.getLogger().info(TextFormat.GREEN + "NeoBuyer " + TextFormat.YELLOW + "v" + this.getDescription().getVersion() + TextFormat.GREEN + " successfully enabled!");
            this.getLogger().info(TextFormat.AQUA + "-----------------------------------");
            this.getLogger().info(TextFormat.YELLOW + "Follow us:");
            this.getLogger().info(TextFormat.BLUE + "• Telegram: " + TextFormat.WHITE + "https://t.me/ForgePlugins");
            this.getLogger().info(TextFormat.BLUE + "• VKontakte: " + TextFormat.WHITE + "https://vk.com/forgeplugin");
            this.getLogger().info(TextFormat.BLUE + "• GitHub: " + TextFormat.WHITE + "https://github.com/Miroshka000/NeoBuyerV2");
            this.getLogger().info(TextFormat.AQUA + "-----------------------------------");
        } catch (Exception e) {
            getLogger().error("Ошибка при включении плагина", e);
        }
    }

    private void startTimerTask() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new TimerTask(this), 0, getConfig().getInt("update-period", 60), TimeUnit.MINUTES);
    }

    public Map<Integer, ShopItem> getShopItems() {
        return shopItems;
    }

    public void loadShopItems() {
        shopItems.clear();
        for (String key : config.getKeys()) {
            if (isItemSection(key)) {
                try {
                    int slot = Integer.parseInt(key);
                    int itemId = config.getInt(key + ".itemId");
                    int count = config.getInt(key + ".count");
                    int price = config.getInt(key + ".price");
                    String url = config.getString(key + ".url", "");

                    ShopItem shopItem = new ShopItem(itemId, count, price, url);
                    shopItems.put(slot, shopItem);
                    getLogger().debug("Загружен предмет: " + itemId + " в слот " + slot);
                } catch (NumberFormatException e) {
                    getLogger().warning("Неверный формат ключа для предмета: " + key);
                }
            }
        }
        getLogger().info("Загружено " + shopItems.size() + " предметов для магазина");
    }
    
    private boolean isItemSection(String key) {
        try {
            Integer.parseInt(key);
            return config.exists(key + ".itemId") && config.exists(key + ".count") && config.exists(key + ".price");
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public AutoBuyerAPI getAutoBuyerAPI() {
        return autoBuyerAPI;
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}