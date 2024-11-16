package com.justgod.zerk;

import com.justgod.zerk.controllers.PlayerController;
import org.bukkit.plugin.java.JavaPlugin;

public final class Zerk extends JavaPlugin {

    private static Zerk _instance;

    public static Zerk getInstance() {
        return _instance;
    };

    @Override
    public void onEnable() {
        saveDefaultConfig();
        _instance = this;
        getServer().getPluginManager().registerEvents(new PlayerController(), this);
    }

    @Override
    public void onDisable() {
    }
}
