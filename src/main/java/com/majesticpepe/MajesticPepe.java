package com.majesticpepe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class MajesticPepe extends JavaPlugin implements Listener {

    private Scoreboard scoreboard;
    private Team pepeTeam, poposhirTeam;
    private Map<UUID, String> playerTeams = new HashMap<>();
    private Map<UUID, Integer> kills = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("shop").setExecutor(this);
        getCommand("team").setExecutor(this);
        getCommand("pepeinfo").setExecutor(this);
        getCommand("kills").setExecutor(this);

        setupScoreboard();
        getLogger().info("§2MajesticPepe loaded! Pepe Gang Must Perish.");
    }

    private void setupScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        pepeTeam = scoreboard.registerNewTeam("pepe_shifu");
        pepeTeam.setPrefix(ChatColor.GREEN + "[PEPE SHIFU] ");
        
        poposhirTeam = scoreboard.registerNewTeam("poposhir_shifu");
        poposhirTeam.setPrefix(ChatColor.DARK_BLUE + "[POPOSHIR SHIFU] ");
        
        for (Player p : Bukkit.getOnlinePlayers()) p.setScoreboard(scoreboard);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        switch (cmd.getName()) {
            case "shop" -> openShop(p);
            case "team" -> {
                if (!sender.hasPermission("op")) {
                    p.sendMessage(ChatColor.RED + "OP only!");
                    return true;
                }
                if (args.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /team <player> <pepe|poposhir|none>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) { p.sendMessage(ChatColor.RED + "Player not found!"); return true; }
                
                String team = args[1].toLowerCase();
                if (team.equals("pepe")) {
                    pepeTeam.addEntry(target.getName());
                    poposhirTeam.removeEntry(target.getName());
                    playerTeams.put(target.getUniqueId(), "pepe");
                    target.setScoreboard(scoreboard);
                    target.sendMessage(ChatColor.GREEN + "Joined PEPE SHIFU!");
                } else if (team.equals("poposhir")) {
                    poposhirTeam.addEntry(target.getName());
                    pepeTeam.removeEntry(target.getName());
                    playerTeams.put(target.getUniqueId(), "poposhir");
                    target.setScoreboard(scoreboard);
                    target.sendMessage(ChatColor.DARK_BLUE + "Joined POPOSHIR SHIFU!");
                } else if (team.equals("none")) {
                    pepeTeam.removeEntry(target.getName());
                    poposhirTeam.removeEntry(target.getName());
                    playerTeams.remove(target.getUniqueId());
                    target.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
                p.sendMessage(ChatColor.GREEN + "Set " + target.getName() + " to team: " + team);
            }
            case "pepeinfo" -> {
                int k = kills.getOrDefault(p.getUniqueId(), 0);
                String team = playerTeams.getOrDefault(p.getUniqueId(), "None");
                p.sendMessage(ChatColor.DARK_GREEN + "=== YOUR STATS ===");
                p.sendMessage(ChatColor.GREEN + "Team: " + team);
                p.sendMessage(ChatColor.GREEN + "Kills: " + ChatColor.YELLOW + k);
            }
            case "kills" -> {
                p.sendMessage(ChatColor.DARK_GREEN + "=== KILL LEADERBOARD ===");
                kills.entrySet().stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                        .limit(10)
                        .forEach(e -> {
                            String name = Bukkit.getOfflinePlayer(e.getKey()).getName();
                            p.sendMessage(ChatColor.YELLOW + name + ": " + ChatColor.WHITE + e.getValue());
                        });
            }
        }
        return true;
    }

    private void openShop(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "PEPE GANG SHOP");
        
        // Fill with green panes
        ItemStack glass = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Shop items
        inv.setItem(10, makeItem(Material.NETHERITE_SWORD, "El Pepe", "3 Emerald + Frogspawn"));
        inv.setItem(11, makeItem(Material.SHIELD, "Pepe Shield", "2 Emerald + Pepe Ore"));
        inv.setItem(12, makeItem(Material.STICK, "Pepe Handle", "1 Netherite + Redstone"));
        inv.setItem(13, makeItem(Material.EMERALD_ORE, "Pepe Ore", "2 Netherite Ingot"));
        inv.setItem(14, makeItem(Material.FISHING_ROD, "Stab Shot", "2 Diamond Block + Rod"));
        inv.setItem(15, makeItem(Material.FIRE_CHARGE, "Nuke Shot", "7 Diamond Block + Rod"));
        inv.setItem(16, makeItem(Material.TNT, "Landmine", "1 Diamond Block + 6 TNT"));

        p.openInventory(inv);
    }

    private ItemStack makeItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + name);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setScoreboard(scoreboard);
        String team = playerTeams.getOrDefault(p.getUniqueId(), "None");
        if (team.equals("pepe")) pepeTeam.addEntry(p.getName());
        else if (team.equals("poposhir")) poposhirTeam.addEntry(p.getName());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("PEPE GANG SHOP")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        p.sendMessage(ChatColor.GREEN + "Item purchased! (Shop is placeholder)");
    }
}
