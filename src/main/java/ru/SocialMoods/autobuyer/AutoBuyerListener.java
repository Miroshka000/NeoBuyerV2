package ru.SocialMoods.autobuyer;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import me.onebone.economyapi.EconomyAPI;
import ru.SocialMoods.Buyer;
import ru.SocialMoods.autobuyer.data.BuyerData;

public class AutoBuyerListener implements Listener {

    private final AutoBuyerAPI autoBuyerAPI;
    private final String salePopupMessage;
    private final Buyer plugin;

    public AutoBuyerListener(AutoBuyerAPI autoBuyerAPI, Buyer plugin) {
        this.autoBuyerAPI = autoBuyerAPI;
        this.plugin = plugin;
        this.salePopupMessage = plugin.getConfig().getString("sale-popup", "Вы продали {item_name} за {amount} монет!");
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getInventory().getHolder();

        if (!autoBuyerAPI.isPlayerConnected(player)) {
            return;
        }

        BuyerData buyerData = autoBuyerAPI.getPlayerData(player);
        Item pickedItem = event.getItem().getItem();
        
        boolean isDirectMatch = pickedItem.getId() == buyerData.item().getId() && 
                               pickedItem.getDamage() == buyerData.item().getDamage();
                               
        boolean isResourceMatch = autoBuyerAPI.isResourcesEnabled() && 
                                 autoBuyerAPI.isResourceItem(pickedItem.getId()) && 
                                 autoBuyerAPI.getProductIdForResource(pickedItem.getId()) == buyerData.item().getId();
                                 
        if (isDirectMatch || isResourceMatch) {
            Inventory inventory = player.getInventory();
            int totalQuantity = pickedItem.getCount();

            inventory.addItem(pickedItem);
            event.setCancelled(true);
            
            int productId = isResourceMatch ? autoBuyerAPI.getProductIdForResource(pickedItem.getId()) : buyerData.item().getId();
            Item productItem = Item.get(productId, 0);
            
            for (Item item : inventory.getContents().values()) {
                if (isMatchingItem(item, pickedItem, isResourceMatch)) {
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
                    if (isMatchingItem(item, pickedItem, isResourceMatch)) {
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

                String itemName = isResourceMatch ? productItem.getName() : pickedItem.getName();
                String popupMessage = salePopupMessage
                        .replace("{item_name}", itemName)
                        .replace("{amount}", String.format("%.2f", totalPrice));

                player.sendPopup(popupMessage);
            }

            event.getItem().kill();
        }
    }
    
    private boolean isMatchingItem(Item inventoryItem, Item pickedItem, boolean isResourceMatch) {
        if (isResourceMatch) {
            return inventoryItem.getId() == pickedItem.getId();
        } else {
            return inventoryItem.getId() == pickedItem.getId() && inventoryItem.getDamage() == pickedItem.getDamage();
        }
    }
}