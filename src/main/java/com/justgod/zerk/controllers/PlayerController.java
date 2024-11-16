package com.justgod.zerk.controllers;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.justgod.zerk.Zerk;
import com.justgod.zerk.enums.NightVisionHelmet;
import com.justgod.zerk.utils.Timeout;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerController implements Listener {

    private void addInventoryItems(Player player) {
        Inventory inventory = player.getInventory();

        if (!inventory.isEmpty())
            return;

        ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemStack helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemStack chestPlate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Material.NETHERITE_BOOTS);

        Map<Enchantment, Integer> pickaxeEnchantments = new HashMap<>();

        pickaxeEnchantments.put(Enchantment.FORTUNE, 3);
        pickaxeEnchantments.put(Enchantment.EFFICIENCY, 4);
        pickaxeEnchantments.put(Enchantment.UNBREAKING, 3);

        pickaxe.addEnchantments(pickaxeEnchantments);

        Map<Enchantment, Integer> swordEnchantments = new HashMap<>();

        swordEnchantments.put(Enchantment.KNOCKBACK, 2);
        swordEnchantments.put(Enchantment.FIRE_ASPECT, 2);
        swordEnchantments.put(Enchantment.UNBREAKING, 3);
        swordEnchantments.put(Enchantment.SHARPNESS, 5);

        sword.addEnchantments(swordEnchantments);

        Map<Enchantment, Integer> armorEnchantments = new HashMap<>();

        armorEnchantments.put(Enchantment.PROTECTION, 4);
        armorEnchantments.put(Enchantment.UNBREAKING, 3);
        armorEnchantments.put(Enchantment.FIRE_PROTECTION, 3);

        helmet.addEnchantments(armorEnchantments);
        chestPlate.addEnchantments(armorEnchantments);
        leggings.addEnchantments(armorEnchantments);
        boots.addEnchantments(armorEnchantments);

        inventory.addItem(pickaxe, sword, helmet, chestPlate, leggings, boots);
    }

    public void showBossBar(Player player) {
        Zerk instance = Zerk.getInstance();
        Logger logger = instance.getLogger();
        BossBar bossBar = BossBar.bossBar(
            Component.text(
                "Welcome to the server",
                TextColor.color(255, 100, 0),
                TextDecoration.BOLD
            ),
            0f,
            BossBar.Color.RED,
            Overlay.PROGRESS
        );
        Thread bossBarInterval = Timeout.setInterval(() -> {
            try {
                float currentProgress = bossBar.progress();
                Color currentColor = bossBar.color();
                bossBar.color(currentColor == BossBar.Color.BLUE ? BossBar.Color.RED : BossBar.Color.BLUE);
                if (currentProgress < 1.0f)
                    bossBar.progress(currentProgress + 0.25f);
                else
                    bossBar.progress(0.0f);
            } catch (Exception e) {
                Timeout.clearInterval();
                logger.warning("Boss bar thread aborted '" + e.getMessage() + "'");
            }
        }, 1000);
        player.showBossBar(bossBar);
        Timeout.setTimeout(() -> {
            if (player != null) {
                player.hideBossBar(bossBar);
            }
            if (bossBarInterval != null) {
                logger.info("Remove bossBar thread for " + player.getName());
                Timeout.clearInterval(bossBarInterval);
            }
        }, 5000);
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.spawnParticle(
            Particle.SMOKE,
            player.getLocation(),
            1000,
            2,
            2,
            2,
            0.1
        );
        player.giveExp(8000);

        showBossBar(player);
        addInventoryItems(player);
    }

    @EventHandler
    public void onPlayerDropped(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Zerk.getInstance().getLogger().info("Goodbye noob " + player.getName());
    }

    @EventHandler
    public void onPlayerEquipHelmet(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        ItemStack oldItem = event.getOldItem();
        ItemStack newItem = event.getNewItem();
        Material oldItemType = oldItem.getType();
        Material newItemType = newItem.getType();
        NightVisionHelmet oldHelmet = NightVisionHelmet.getByMaterial(oldItemType);
        NightVisionHelmet newHelmet = NightVisionHelmet.getByMaterial(newItemType);

        if (oldHelmet != null || newHelmet != null) {
            Zerk instance = Zerk.getInstance();
            Logger logger = instance.getLogger();
            FileConfiguration config = instance.getConfig();
            PotionEffectType nightVision = PotionEffectType.NIGHT_VISION;
            List<String> helmetConfig = config.getStringList("night-vision-helmets");
            Boolean configHasOldHelmet = oldHelmet != null ? helmetConfig.contains(oldHelmet.getName()) : false;
            Boolean configHasNewHelmet = newHelmet != null ? helmetConfig.contains(newHelmet.getName()) : false;

            logger.info("Swap armor helmet triggered for player " + player.getName());

            if (configHasOldHelmet && !configHasNewHelmet) {
                if (player.hasPotionEffect(nightVision)) {
                    logger.info("Player " + player.getName() + " has no longer netherite helmet, removing night vision");
                    player.removePotionEffect(nightVision);
                }
            } else if (configHasNewHelmet && !configHasOldHelmet) {
                if (!player.hasPotionEffect(nightVision)) {
                    logger.info("Player " + player.getName() + " has equiped netherite helmet, adding night vision");
                    player.addPotionEffect(
                        new PotionEffect(
                            nightVision,
                            PotionEffect.INFINITE_DURATION,
                            1000
                        )
                    );
                }
            }
        }
    }

}
