package ru.SocialMoods.autobuyer;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import ru.SocialMoods.autobuyer.data.BuyerData;

import java.util.HashMap;
import java.util.Map;

public class AutoBuyerAPI {

    private final Map<String, BuyerData> buyers;

    public AutoBuyerAPI() {
        this.buyers = new HashMap<>();
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
}