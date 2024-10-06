package ru.SocialMoods.autobuyer;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.item.Item;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.inventory.Inventory;
import ru.SocialMoods.Buyer;
import ru.SocialMoods.autobuyer.data.BuyerData;
import me.onebone.economyapi.EconomyAPI;

public class AutoBuyerListener implements Listener {

    private final AutoBuyerAPI autoBuyerAPI;
    private final String salePopupMessage;

    public AutoBuyerListener(AutoBuyerAPI autoBuyerAPI, Buyer plugin) {
        this.autoBuyerAPI = autoBuyerAPI;

        this.salePopupMessage = plugin.getConfig().getString("messages.sale_popup", "Вы продали {item_name} за {amount} монет!");
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        Player player = (Player) event.getInventory().getHolder();

        if (!autoBuyerAPI.isPlayerConnected(player)) {
            return;
        }

        BuyerData buyerData = autoBuyerAPI.getPlayerData(player);
        Item pickedItem = event.getItem().getItem();

        if (pickedItem.getId() == buyerData.item().getId() && pickedItem.getDamage() == buyerData.item().getDamage()) {
            Inventory inventory = player.getInventory();
            int totalQuantity = pickedItem.getCount();

            inventory.addItem(pickedItem);
            event.setCancelled(true);

            for (Item item : inventory.getContents().values()) {
                if (item.getId() == buyerData.item().getId() && item.getDamage() == buyerData.item().getDamage()) {
                    totalQuantity += item.getCount();
                }
            }

            if (totalQuantity >= buyerData.quantity()) {
                int numberOfSales = totalQuantity / buyerData.quantity();
                double totalPrice = numberOfSales * buyerData.pricePerItem();
                EconomyAPI.getInstance().addMoney(player, totalPrice);

                int quantityToRemove = numberOfSales * buyerData.quantity();

                for (int slot = 0; slot < inventory.getSize(); slot++) {
                    Item item = inventory.getItem(slot);
                    if (item.getId() == buyerData.item().getId() && item.getDamage() == buyerData.item().getDamage()) {
                        int itemCount = item.getCount();
                        if (itemCount <= quantityToRemove) {
                            quantityToRemove -= itemCount;
                            inventory.clear(slot);
                        } else {
                            item.setCount(itemCount - quantityToRemove);
                            inventory.setItem(slot, item);
                            quantityToRemove = 0;
                        }
                        if (quantityToRemove <= 0) {
                            break;
                        }
                    }
                }

                Level level = player.getLocation().getLevel();
                level.addSound(player.getLocation(), Sound.RANDOM_ORB);

                String itemName = pickedItem.getName();
                String popupMessage = salePopupMessage
                        .replace("{item_name}", itemName)
                        .replace("{amount}", String.format("%.2f", totalPrice));

                player.sendPopup(popupMessage);
            }

            event.getItem().kill();
        }
    }
}