package de.xaaronzz105.chunkLoader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    public final static Logger LOGGER = Bukkit.getLogger();
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("chunkloaders").setExecutor(new chunkLoadersCommand());
        getCommand("chunkloaders").setTabCompleter(new chunkLoadersCommand());
        Bukkit.getPluginManager().registerEvents(new chunkLoaderHandler(), this);

        chunkLoaderHandler.init();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, chunkLoaderHandler::tick, 0L, 2L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, chunkLoaderHandler::performanceTick, 0L, 60L);

        LOGGER.info("---------------------------");
        LOGGER.info("Chunk loader plugin loaded!");
        LOGGER.info("---------------------------");
    }

    @Override
    public void onDisable() {
    }

    public static Main getInstance() {
        return instance;
    }
}
