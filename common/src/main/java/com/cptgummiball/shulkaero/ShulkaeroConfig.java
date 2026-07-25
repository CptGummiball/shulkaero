package com.cptgummiball.shulkaero;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;

/**
 * Shulkaero client config, stored as plain JSON at config/shulkaero/config.json
 * (dependency-free on purpose). Loaded lazily, written with defaults on first run.
 */
public final class ShulkaeroConfig {

    public static final String FEEDBACK_ACTIONBAR = "actionbar";
    public static final String FEEDBACK_CHAT = "chat";
    public static final String FEEDBACK_NONE = "none";

    /** Master switch (also toggled by /shulkaero toggle). */
    public boolean enabled = true;
    /** Only create waypoints for boxes with a custom (anvil) name. */
    public boolean onlyNamedBoxes = false;
    /** Keep the waypoint after breaking until the dropped box item is picked up. */
    public boolean waitForPickup = true;
    /** Radius (blocks) around the break position to look for the dropped box item. */
    public int pickupSearchRadius = 8;
    /** How often (in client ticks) tracked positions are checked. */
    public int checkIntervalTicks = 10;
    /** "actionbar", "chat" or "none". */
    public String feedback = FEEDBACK_ACTIONBAR;
    /** Put shulker waypoints into their own waypoint set instead of the active one. */
    public boolean useOwnWaypointSet = false;
    public String waypointSetName = "Shulkaero";
    /** Derive the waypoint color from a literal §-color code in the box name (fallback: box color). */
    public boolean colorFromNameCodes = false;
    /** Prefix prepended to every waypoint name, e.g. "[Box] ". */
    public String namePrefix = "";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ShulkaeroConfig instance;

    public static ShulkaeroConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ShulkaeroConfig reload() {
        instance = load();
        return instance;
    }

    public void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            Shulkaero.LOGGER.warn("Could not save {}", file, e);
        }
    }

    private static ShulkaeroConfig load() {
        Path file = file();
        ShulkaeroConfig config = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, ShulkaeroConfig.class);
            } catch (IOException | RuntimeException e) {
                Shulkaero.LOGGER.warn("Could not read {}, using defaults", file, e);
            }
        }
        boolean writeBack = config == null;
        if (config == null) {
            config = new ShulkaeroConfig();
        }
        config.sanitize();
        if (writeBack) {
            config.save();
        }
        return config;
    }

    private void sanitize() {
        pickupSearchRadius = Math.max(1, Math.min(32, pickupSearchRadius));
        checkIntervalTicks = Math.max(1, Math.min(200, checkIntervalTicks));
        if (!FEEDBACK_ACTIONBAR.equals(feedback) && !FEEDBACK_CHAT.equals(feedback) && !FEEDBACK_NONE.equals(feedback)) {
            feedback = FEEDBACK_ACTIONBAR;
        }
        if (waypointSetName == null || waypointSetName.isBlank()) {
            waypointSetName = "Shulkaero";
        }
        if (namePrefix == null) {
            namePrefix = "";
        }
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve(Shulkaero.MOD_ID).resolve("config.json");
    }
}
