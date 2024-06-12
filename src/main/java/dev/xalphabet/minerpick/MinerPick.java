package dev.xalphabet.minerpick;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;

public class MinerPick extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        this.getCommand("givepickaxe").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("givepickaxe")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be run by a player.");
                return true;
            }

            Player player = (Player) sender;
            ItemStack pickaxe = getConfiguredPickaxe();
            player.getInventory().addItem(pickaxe);
            player.sendMessage(ChatColor.GREEN + "You have been given a special pickaxe!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.getType().equals(Material.valueOf(getConfig().getString("pickaxe.type").toUpperCase()))) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', getConfig().getString("pickaxe.name")))) {
            return;
        }

        // Add the 3x3 digging logic here
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    Block block = event.getBlock().getRelative(x, y, z);
                    block.breakNaturally(item);
                }
            }
        }
    }

    private ItemStack getConfiguredPickaxe() {
        String type = getConfig().getString("pickaxe.type");
        String name = getConfig().getString("pickaxe.name");
        String lore = getConfig().getString("pickaxe.lore");
        ItemStack pickaxe = new ItemStack(Material.valueOf(type.toUpperCase()));
        ItemMeta meta = pickaxe.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', lore).split("\\|")));

        for (String enchantment : getConfig().getConfigurationSection("pickaxe.enchantments").getKeys(false)) {
            int level = getConfig().getInt("pickaxe.enchantments." + enchantment);
            meta.addEnchant(Enchantment.getByName(enchantment.toUpperCase()), level, true);
        }

        pickaxe.setItemMeta(meta);
        return pickaxe;
    }
}