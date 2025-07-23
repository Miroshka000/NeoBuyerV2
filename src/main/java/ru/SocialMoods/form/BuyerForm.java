package ru.SocialMoods.form;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import com.formconstructor.form.CustomForm;
import com.formconstructor.form.SimpleForm;

import com.formconstructor.form.element.custom.Slider;
import com.formconstructor.form.element.custom.Toggle;
import com.formconstructor.form.element.general.Label;
import com.formconstructor.form.element.simple.ImageType;
import me.onebone.economyapi.EconomyAPI;
import ru.SocialMoods.Buyer;
import ru.SocialMoods.model.ShopItem;
import ru.SocialMoods.util.PriceCalculator;

import java.util.Iterator;
import java.util.Map.Entry;

public class BuyerForm {
    public static void openBuyerForm(Player player, Buyer plugin) {
        String formTitle = plugin.getConfig().getString("buyer-form.title");
        String formContentTemplate = plugin.getConfig().getString("buyer-form.content");
        String buttonTemplate = plugin.getConfig().getString("buyer-form.button-template");
        plugin.getConfig().getString("buyer-form.messages.not-enough-items");
        String tutorialButton = plugin.getConfig().getString("buyer-form.tutorial-button");

        SimpleForm form = new SimpleForm(formTitle);
        Iterator<Entry<Integer, ShopItem>> var4 = plugin.getShopItems().entrySet().iterator();
        PriceCalculator priceCalculator = new PriceCalculator(plugin);

        form.addContent(formContentTemplate);

        while (var4.hasNext()) {
            Entry<Integer, ShopItem> entry = var4.next();
            ShopItem shopItem = entry.getValue();
            int adjustedPrice = priceCalculator.getAdjustedPrice(shopItem);
            String buttonText = buttonTemplate
                    .replace("{item_name}", shopItem.getItemName())
                    .replace("{item_count}", String.valueOf(shopItem.getCount()))
                    .replace("{price}", String.valueOf(adjustedPrice));
                    
            String imageUrl = shopItem.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                form.addButton(buttonText, ImageType.PATH, imageUrl, (pl, button) -> 
                    openItemForm(pl, plugin, shopItem, adjustedPrice));
            } else {
                form.addButton(buttonText, (pl, button) -> 
                    openItemForm(pl, plugin, shopItem, adjustedPrice));
            }
        }
        
        form.addButton(tutorialButton, (pl, button) -> 
            ShowTutorialForm(pl, plugin));
        
        form.setCloseHandler(pl -> {});
        
        form.send(player);
    }
    
    private static void openItemForm(Player player, Buyer plugin, ShopItem shopItem, int adjustedPrice) {
        String successMsg = plugin.getConfig().getString("buyer-form.messages.success");

        CustomForm itemForm = new CustomForm(shopItem.getItemName());
        PriceCalculator priceCalculator = new PriceCalculator(plugin);

        int itemCountInInventory = 0;
        for (Item item : player.getInventory().getContents().values()) {
            if (item.getId() == shopItem.getItem().getId()) {
                itemCountInInventory += item.getCount();
            }
        }

        itemForm.addElement(new Label(plugin.getConfig().getString("buyer-form.item-form-content")
                .replace("{item}", shopItem.getItemName())
                .replace("{price}", String.valueOf(shopItem.getPrice()))
                .replace("{available}", String.valueOf(itemCountInInventory))));

        itemForm.addElement("slider", new Slider("Выберите количество предметов:", 1f, Math.min(64, itemCountInInventory), 1, 1));

        itemForm.setHandler((pl, response) -> {
            if (response == null) {
                return;
            }

            float count = response.getSlider("slider").getValue();
            int countInt = (int) count;

            int totalSold = 0;

            for (Item item : pl.getInventory().getContents().values()) {
                if (item.getId() == shopItem.getItem().getId()) {
                    if (item.getCount() >= countInt) {
                        item.setCount(item.getCount() - countInt);
                        totalSold += countInt;
                        break;
                    } else {
                        countInt -= item.getCount();
                        totalSold += item.getCount();
                        item.setCount(0);
                    }
                }
            }

            if (totalSold > 0) {
                pl.getInventory().sendContents(pl);
                EconomyAPI.getInstance().addMoney(pl, adjustedPrice * totalSold);
                Level level = player.getLocation().getLevel();
                level.addSound(player.getLocation(), Sound.RANDOM_ORB);
                pl.sendMessage(successMsg.replace("{count}", String.valueOf(totalSold)));
                openBuyerForm(player, plugin);
                priceCalculator.incrementSalesCount(shopItem.getItem().getId());
            } else {
                pl.sendMessage(plugin.getConfig().getString("buyer-form.messages.not-enough-items"));
            }
        });
        
        itemForm.send(player);
    }

    public static void ShowTutorialForm(Player player, Buyer plugin) {
        double ratio = plugin.getConfig().getDouble("price-ratio");
        double level = plugin.getConfig().getInt("level-ratio");

        String tutorialTemplate = plugin.getConfig().getString("buyer-form.tutorial-content");
        SimpleForm tutorial = new SimpleForm(plugin.getConfig().getString("buyer-form.tutorial-title"));

        String formattedRatio = String.format("%.0f%%", ratio * 100);
        String levelRatio = String.format("%.0f%%", level * 100);

        String tutorialContent = tutorialTemplate
                .replace("{ratio}", formattedRatio)
                .replace("{level-ratio}", levelRatio);

        tutorial.addContent(tutorialContent);
        
        tutorial.addButton("Настройки", (pl, button) -> 
            openSettingsForm(pl, plugin));
            
        tutorial.addButton("Назад", (pl, button) -> 
            openBuyerForm(pl, plugin));
            
        tutorial.send(player);
    }

    public static void openSettingsForm(Player player, Buyer plugin) {
        String formTitle = plugin.getConfig().getString("settings-form.title");

        CustomForm form = new CustomForm(formTitle);

        boolean isAutoBuyerEnabled = plugin.getAutoBuyerAPI().isPlayerConnected(player);

        String status = isAutoBuyerEnabled ? "Подключен" : "Отключен";
        form.addElement(new Label(plugin.getConfig().getString("settings-form.status-label").replace("{status}", status)));

        form.addElement("toggle", new Toggle(plugin.getConfig().getString("settings-form.toggle-label"), isAutoBuyerEnabled));

        form.setHandler((pl, response) -> {
            if (response == null) {
                return;
            }

            boolean isEnabled = response.getToggle("toggle").getValue();

            if (isEnabled) {
                for (ShopItem shopItem : plugin.getShopItems().values()) {
                    plugin.getAutoBuyerAPI().connectPlayer(pl, shopItem.getItem(), shopItem.getCount(), shopItem.getPrice());
                }
                pl.sendMessage(plugin.getConfig().getString("settings-form.messages.enabled"));
            } else {
                plugin.getAutoBuyerAPI().disconnectPlayer(pl);
                pl.sendMessage(plugin.getConfig().getString("settings-form.messages.disabled"));
            }
            openSettingsForm(pl, plugin);
        });

        form.send(player);
    }
}