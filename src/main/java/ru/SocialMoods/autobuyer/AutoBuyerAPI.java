package ru.SocialMoods.autobuyer;

import java.util.HashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import ru.SocialMoods.Buyer;
import ru.SocialMoods.autobuyer.data.BuyerData;

public class AutoBuyerAPI {

    private final Map<String, BuyerData> buyers;
    private final Map<Integer, Integer> resourceToProductMap;
    private final boolean resourcesEnabled;

    public AutoBuyerAPI(Buyer plugin) {
        this.buyers = new HashMap<>();
        this.resourceToProductMap = new HashMap<>();
        this.resourcesEnabled = plugin.getConfig().getBoolean("auto-buyer.resources-enabled", false);

        if (resourcesEnabled) {
            loadResourceMapping(plugin);
        }
    }

    private void loadResourceMapping(Buyer plugin) {
        if (plugin.getConfig().exists("auto-buyer.resources")) {
            Map<String, Object> resources = plugin.getConfig().getSection("auto-buyer.resources").getAllMap();
            
            for (Map.Entry<String, Object> entry : resources.entrySet()) {
                try {
                    int resourceId = Integer.parseInt(entry.getKey());
                    int productId = 0;
                    
                    if (entry.getValue() instanceof Integer) {
                        productId = (Integer) entry.getValue();
                    } else if (entry.getValue() instanceof String) {
                        productId = Integer.parseInt((String) entry.getValue());
                    } else if (entry.getValue() instanceof Long) {
                        productId = ((Long) entry.getValue()).intValue();
                    } else {
                        plugin.getLogger().warning("Неизвестный тип значения для ключа " + entry.getKey() + ": " + entry.getValue().getClass().getName());
                        continue;
                    }
                    
                    resourceToProductMap.put(resourceId, productId);
                    plugin.getLogger().debug("Загружено сопоставление ресурса: " + resourceId + " -> " + productId);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Невозможно преобразовать ключ или значение в число: " + entry.getKey() + " -> " + entry.getValue());
                }
            }
            
            plugin.getLogger().info("Загружено " + resourceToProductMap.size() + " сопоставлений ресурсов");
        } else {
            plugin.getLogger().warning("Секция auto-buyer.resources не найдена в конфигурации");
        }
    }

    public void connectPlayer(Player player, Item item, int quantity, double pricePerItem) {
        buyers.put(player.getName(), new BuyerData(item, quantity, pricePerItem));
    }

    public void disconnectPlayer(Player player) {
        buyers.remove(player.getName());
    }

    public boolean isPlayerConnected(Player player) {
        return buyers.containsKey(player.getName());
    }

    public BuyerData getPlayerData(Player player) {
        return buyers.get(player.getName());
    }
    
    public boolean isResourcesEnabled() {
        return resourcesEnabled;
    }
    
    public boolean isResourceItem(int itemId) {
        return resourcesEnabled && resourceToProductMap.containsKey(itemId);
    }
    
    public Integer getProductIdForResource(int resourceId) {
        return resourceToProductMap.get(resourceId);
    }
}