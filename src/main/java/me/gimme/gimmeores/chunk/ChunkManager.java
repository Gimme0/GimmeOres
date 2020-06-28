package me.gimme.gimmeores.chunk;

import me.gimme.gimmecore.util.SaveFile;
import me.gimme.gimmeores.generation.Populator;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Handles newly generated chunks by applying populators on them.
 */
public class ChunkManager implements Listener {
    private static final String CONFIG_DEFAULT_VALUES = "default";
    private static final String CONFIG_POPULATORS = "populators";

    private static final String FILE_PATH = "populated-chunks.json";

    private Plugin plugin;
    private FileConfiguration config;
    private SaveFile<PopulatedChunksData> saveFile;
    private Map<UUID, Random> randomGeneratorByWorld = new HashMap<>();

    private Map<UUID, Integer> chunkCountByWorld = new HashMap<>();
    private PopulatedChunksData populatedChunksData = new PopulatedChunksData();
    private List<Populator> populators = new ArrayList<>();

    public ChunkManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.saveFile = new SaveFile<>(plugin, FILE_PATH, PopulatedChunksData.class);

        registerPopulators();
    }

    /**
     * Loads data from the save file and starts autosaving.
     */
    public void onEnable() {
        PopulatedChunksData data = saveFile.load();
        if (data != null) this.populatedChunksData = data;

        saveFile.autosave(plugin, 20 * 300, () -> populatedChunksData);
    }

    /**
     * Saves data to the save file.
     */
    public void onDisable() {
        populatedChunksData.cleanupWorlds(plugin.getServer());
        saveFile.save(populatedChunksData);
    }

    /**
     * Creates and registers all populators from the config.
     */
    @SuppressWarnings("unchecked")
    private void registerPopulators() {
        List<?> populatorSettingsList = config.getList(CONFIG_POPULATORS);
        if (populatorSettingsList == null) return;

        ConfigurationSection ds = config.getConfigurationSection(CONFIG_DEFAULT_VALUES);

        for (Map<String, ?> ps : (List<Map<String, ?>>) populatorSettingsList) {
            Populator populator = new PopulatorSettings(ps, ds, plugin, populatedChunksData).createPopulator();
            if (populator != null) this.populators.add(populator);
        }
    }

    /**
     * Populates chunks when they are first generated with the custom populators.
     *
     * @param event the chunk load event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event) {
        int cx = event.getChunk().getX();
        int cz = event.getChunk().getZ();

        for (int x = cx - 1; x <= cx; x++) {
            for (int z = cz - 1; z <= cz; z++) {
                boolean neighborsLoaded = true;
                for (int neighborX = x; neighborX <= x + 1; neighborX++) {
                    for (int neighborZ = z; neighborZ <= z + 1; neighborZ++) {
                        if (!event.getWorld().isChunkLoaded(neighborX, neighborZ)) neighborsLoaded = false;
                    }
                }
                if (neighborsLoaded) onDecoratorChunkGeneration(event.getWorld().getChunkAt(x, z));
            }
        }
    }

    /**
     * Populates chunks when they are first generated with the custom decoration populators.
     *
     * @param chunk the chunk that was loaded
     */
    private void onDecoratorChunkGeneration(@NotNull Chunk chunk) {
        World world = chunk.getWorld();

        Set<Point> populatedChunks = populatedChunksData.getPopulatedChunks(world);

        Point chunkPoint = new Point(chunk.getX(), chunk.getZ());
        if (populatedChunks.contains(chunkPoint)) return;
        populatedChunks.add(chunkPoint);

        Bukkit.getLogger().info(world.getName() + ": populating new chunk ("
                + chunkCountByWorld.merge(world.getUID(), 1, Integer::sum) + ")"); // TODO: remove?

        for (Populator populator : populators) {
            populator.populate(chunk, getRandomGenerator(world));
        }
    }

    /**
     * @return the random generator for the specified world
     * @param world the world to get the random generator for
     */
    @NotNull
    private Random getRandomGenerator(@NotNull World world) {
        return randomGeneratorByWorld.computeIfAbsent(world.getUID(), k -> new Random(populatedChunksData.getSeed(world)));
    }
}
