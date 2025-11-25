package de.xaaronzz105.chunkLoader;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public enum ChunkLoaders {
    one(1, Material.COBBLESTONE, Material.DIAMOND, new ParticleData(Particle.WAX_OFF, 2, new Vector(0.2, 0.2, 0.2), 0.1)),
    three(3, Material.COPPER_INGOT, Material.DIAMOND, new ParticleData(Particle.HAPPY_VILLAGER, 2, new Vector(0.2, 0.2, 0.2), 0.1)),
    five(5, Material.IRON_INGOT, Material.DIAMOND, new ParticleData(Particle.CRIT, 3, new Vector(0.2, 0.2, 0.2), 0.05));

    public final int area;
    public final Recipe recipe;
    public final ParticleData particleData;
    public final ItemStack itemStack;
    ChunkLoaders(int area, Material cheapMaterial, Material expensiveMaterial, ParticleData particleData) {
        ItemStack result = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setDisplayName("§6Chunk loader");
        resultMeta.setItemName("chunk_loader_" + area);
        resultMeta.setLore(List.of("§7Lädt " + area + "x" + area + " chunks"));
        result.setItemMeta(resultMeta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Main.getInstance(), "chunk_loader_" + area), result);
        recipe.shape(
                "OCO",
                "CEC",
                "OCO"
        );
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('C', cheapMaterial);
        recipe.setIngredient('E', expensiveMaterial);

        this.area = area;
        this.particleData = particleData;
        this.recipe = recipe;
        this.itemStack = result;
    }

    public static List<ChunkLoaders> getLoaderById(String id) {
        List<ChunkLoaders> loaders = new ArrayList<>();
        for (ChunkLoaders loader : ChunkLoaders.values()) if (id.equals("chunk_loader_" + loader.area)) loaders.add(loader);
        return loaders;
    }

    public static ChunkLoaders safeValueOf(String name) {
        for (ChunkLoaders loader : ChunkLoaders.values()) {
            if (loader.name().equals(name)) return loader;
        }
        return null;
    }
}
