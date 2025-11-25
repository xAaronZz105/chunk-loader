package de.xaaronzz105.chunkLoader;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class chunkLoaderHandler implements Listener {
    public static FileConfiguration config;
    private static final HashMap<Player, HashMap<Location, LoaderData>> showingBorders = new HashMap<>();

    static void init() {
        Main main = Main.getInstance();

        for (ChunkLoaders chunkLoader : ChunkLoaders.values()) Bukkit.addRecipe(chunkLoader.recipe);

        main.saveDefaultConfig();
        config = main.getConfig();
    }
    static void tick() {
        List<Map<?, ?>> loaderList = config.getMapList("loaders");

        for (Map<?, ?> loader : loaderList) {
            LoaderData data = LoaderData.extract(loader);
            if (data == null) {
                continue;
            }
            Location location = data.location();
            World world = location.getWorld();

            ParticleData particleData = data.loaderType().particleData;
            world.spawnParticle(particleData.particle(), location.add(0.5, 0.5, 0.5), particleData.particleAmount(), particleData.delta().getX(), particleData.delta().getY(), particleData.delta().getZ(), particleData.speed());
        }
    }
    static void performanceTick() {
        List<Map<?, ?>> loaderList = config.getMapList("loaders");
        List<Map<?, ?>> refreshedLoaderList = loaderList.stream()
                .map(HashMap::new)
                .collect(Collectors.toList());

        for (Map<?, ?> loader : loaderList) {
            LoaderData data = LoaderData.extract(loader);
            if (data == null) {
                refreshedLoaderList.remove(loader);
                continue;
            }

            forceLoadChunk(data, true);
        }

        if (loaderList.size() != refreshedLoaderList.size()) {
            config.set("loaders", refreshedLoaderList);
            Main.getInstance().saveConfig();
        }

        if (!showingBorders.isEmpty()) {
            for (Player player : showingBorders.keySet()) {
                HashMap<Location, LoaderData> loaderDatas = showingBorders.get(player);

                for (Location location : loaderDatas.keySet()) {
                    //LoaderData data = loaderDatas.get(location);

                    player.spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0.05);
                }
            }
        }
    }

    @EventHandler
    private static void onChunkLoaderPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.STRUCTURE_VOID && event.getBlockReplacedState().getType() != Material.STRUCTURE_VOID) return;

        if (event.getBlockReplacedState().getType() == Material.STRUCTURE_VOID) {
            LoaderData loaderData = LoaderData.getLoaderByLocation(event.getBlock().getLocation());
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), loaderData.loaderType().itemStack);
            removeLoader(loaderData);
            forceLoadChunk(loaderData, false);
            return;
        }
        List<ChunkLoaders> loadersList = ChunkLoaders.getLoaderById(event.getItemInHand().getItemMeta().getItemName());

        if (loadersList.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        ChunkLoaders loader = loadersList.getFirst();
        addLoader(new LoaderData(event.getBlockPlaced().getLocation(), loader));
    }
    @EventHandler
    private static void onChunkLoaderBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STRUCTURE_VOID) return;

        LoaderData loaderData = LoaderData.getLoaderByLocation(event.getBlock().getLocation());
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), loaderData.loaderType().itemStack);
        removeLoader(loaderData);
        forceLoadChunk(loaderData, false);
    }
    @EventHandler
    private static void onChunkLoaderRightClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.getClickedBlock().getType() != Material.STRUCTURE_VOID) return;
        LoaderData loaderData = LoaderData.getLoaderByLocation(event.getClickedBlock().getLocation());
        //System.out.println("clicked chunk loader, Type: " + loaderData.loaderType().name());

        HashMap<Location, LoaderData> particleHashMap = showingBorders.computeIfAbsent(event.getPlayer(), k -> new HashMap<>());

        particleHashMap.put(event.getClickedBlock().getLocation(), loaderData);
    }
    @EventHandler
    private static void onEntityMove(EntityMoveEvent event) {
        if (event.getEntity().getType() != EntityType.ITEM) return;
        System.out.println();
    }

    private static void addLoader(LoaderData loaderData) {
        Location location = loaderData.location();
        World world = location.getWorld();

        Map<String, Object> newLoader = new HashMap<>();
        newLoader.put("x", (int) location.getX());
        newLoader.put("y", (int) location.getY());
        newLoader.put("z", (int) location.getZ());
        newLoader.put("worldName", world.getKey().asString());
        newLoader.put("loaderType", loaderData.loaderType().name());

        List<Map<?, ?>> loaders = config.getMapList("loaders");
        loaders.add(newLoader);

        config.set("loaders", loaders);
        Main.getInstance().saveConfig();
    }
    private static void removeLoader(LoaderData loaderData) {
        Location location = loaderData.location();
        String worldString = location.getWorld().getKey().asString();

        List<Map<?, ?>> loaders = config.getMapList("loaders");

        loaders.removeIf(loader ->
                loader.get("worldName") instanceof String && (loader.get("worldName")).equals(worldString) &&
                        loader.get("x") instanceof Integer && (int) loader.get("x") == (int) location.getX() &&
                        loader.get("y") instanceof Integer && (int) loader.get("y") == (int) location.getY() &&
                        loader.get("z") instanceof Integer && (int) loader.get("z") == (int) location.getZ()
        );

        config.set("loaders", loaders);
        Main.getInstance().saveConfig();
    }

    private static void forceLoadChunk(LoaderData loaderData, boolean forceLoad) {
        World world = loaderData.location().getWorld();
        Location location = loaderData.location();
        int area = loaderData.loaderType().area;

        int chunkX = world.getChunkAt(location).getX() - (area - 1) / 2;
        int chunkZ = world.getChunkAt(location).getZ() - (area - 1) / 2;

        for (int xIt = 0; xIt < area; xIt++) {
            for (int zIt = 0; zIt < area; zIt++) {
                world.getChunkAt(xIt + chunkX, zIt + chunkZ).setForceLoaded(forceLoad);
            }
        }
    }

    private static List<Location> getChunkOutlineForLoader(LoaderData loaderData) {
        Chunk centerChunk = loaderData.location().getChunk();
        int radius = (loaderData.loaderType().area - 1) / 2;
        int y = (int) loaderData.location().getY();

        List<Location> outline = new ArrayList<>();
        World world = centerChunk.getWorld();
        int centerX = centerChunk.getX();
        int centerZ = centerChunk.getZ();

        int minChunkX = centerX - radius;
        int maxChunkX = centerX + radius;
        int minChunkZ = centerZ - radius;
        int maxChunkZ = centerZ + radius;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int blockX = chunkX << 4;
                int blockZ = chunkZ << 4;

                boolean onNorthEdge = chunkZ == minChunkZ;
                boolean onSouthEdge = chunkZ == maxChunkZ;
                boolean onWestEdge  = chunkX == minChunkX;
                boolean onEastEdge  = chunkX == maxChunkX;

                if (onNorthEdge) {
                    for (int i = 0; i < 16; i++)
                        outline.add(new Location(world, blockX + i, y, blockZ));
                }

                if (onSouthEdge) {
                    for (int i = 0; i < 16; i++)
                        outline.add(new Location(world, blockX + i, y, blockZ + 15));
                }

                if (onWestEdge) {
                    for (int i = 0; i < 16; i++)
                        outline.add(new Location(world, blockX, y, blockZ + i));
                }

                if (onEastEdge) {
                    for (int i = 0; i < 16; i++)
                        outline.add(new Location(world, blockX + 15, y, blockZ + i));
                }
            }
        }

        return outline;
    }
}