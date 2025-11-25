package de.xaaronzz105.chunkLoader;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.List;
import java.util.Map;


public record LoaderData(Location location, ChunkLoaders loaderType) {
    public static LoaderData extract(Map<?, ?> loader) {
        if (!(loader.containsKey("x") && loader.containsKey("y") && loader.containsKey("z") && loader.containsKey("worldName") && loader.containsKey("loaderType")))
            return null;


        try {
            int x = (int) loader.get("x");
            int y = (int) loader.get("y");
            int z = (int) loader.get("z");
            String worldString = String.valueOf(loader.get("worldName"));
            String loaderTypeName = String.valueOf(loader.get("loaderType"));

            String[] worldSplit = worldString.split(":");
            World world = Bukkit.getWorld(new NamespacedKey(worldSplit[0], worldSplit[1]));
            if (world == null) {
                return null;
            }

            ChunkLoaders loaderType = ChunkLoaders.valueOf(loaderTypeName);

            return new LoaderData(new Location(world, x, y, z), loaderType);
        } catch (Exception ignore) {
            return null;
        }
    }
    public static LoaderData getLoaderByLocation(Location location) {
        List<Map<?, ?>> loaderList = Main.getInstance().getConfig().getMapList("loaders");
        for (Map<?, ?> loader : loaderList) {
            LoaderData data = LoaderData.extract(loader);
            if (data == null) {
                continue;
            }
            Location loaderLocation = data.location();
            if (loaderLocation.equals(location)) return data;
        }
        return new LoaderData(location, null);
    }
}