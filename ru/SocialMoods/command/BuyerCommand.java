package ru.SocialMoods.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;
import ru.SocialMoods.Buyer;
import ru.SocialMoods.form.BuyerForm;

public class BuyerCommand extends Command {

    private final Buyer plugin;

    public BuyerCommand(Buyer plugin) {
        super("buyer", "Открыть меню скупщика", "/buyer");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player player) {
            BuyerForm.openBuyerForm(player, plugin);
            return true;
        }
        return false;
    }
}