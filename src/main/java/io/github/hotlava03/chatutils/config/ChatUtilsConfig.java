package io.github.hotlava03.chatutils.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.util.function.Function;

public class ChatUtilsConfig {
    private static final Gson gson = new Gson();
    public static final Value<String> COPY_TO_CLIPBOARD_MESSAGE = new Value<>("copyToClipboardMessage",
            "&9Click to copy to clipboard.");
    public static final Value<Boolean> PREVIEW_CONTENT = new Value<>("previewContent", true);
    public static final Value<Boolean> COPY_COLORS = new Value<>("copyColorsIfAny", false);
    public static final Value<Boolean> ANTI_SPAM = new Value<>("antiSpam", true);
    public static final Value<Boolean> TOOLTIP_ENABLED = new Value<>("tooltipEnabled", true);
    public static final Value<Boolean> ENABLED = new Value<>("enabled", true);

    private static File getConfigDirectory() {
        File configDir = new File(MinecraftClient.getInstance().runDirectory, "config");
        if (!configDir.isDirectory() && configDir.exists())
            LogManager.getLogger().warn("[chat-utils] A file was found in place of the config folder!");
        else if (!configDir.isDirectory()) {
            boolean created = configDir.mkdir();
            if (!created) {
                LogManager.getLogger().warn("[chat-utils] Failed to create config folder! This may cause errors!");
            }
        }
        return configDir;
    }

    public static void loadFromFile() {
        File configFile = new File(getConfigDirectory(), "chatutils.json");
        try (FileReader fileReader = new FileReader(configFile)) {
            JsonElement element = gson.fromJson(fileReader, JsonElement.class);

            if (element != null && element.isJsonObject()) {
                if (element.getAsJsonObject().get("ChatUtils").isJsonObject()) {
                    JsonObject root = element.getAsJsonObject().get("ChatUtils").getAsJsonObject();
                    // Read values into the Value classes.
                    COPY_TO_CLIPBOARD_MESSAGE.read(root.get("copyToClipboardMessage"), JsonElement::getAsString);
                    PREVIEW_CONTENT.read(root.get("previewContent"), JsonElement::getAsBoolean);
                    COPY_COLORS.read(root.get("copyColorsIfAny"), JsonElement::getAsBoolean);
                    ANTI_SPAM.read(root.get("antiSpam"), JsonElement::getAsBoolean);
                    TOOLTIP_ENABLED.read(root.get("tooltipEnabled"), JsonElement::getAsBoolean);
                    ENABLED.read(root.get("enabled"), JsonElement::getAsBoolean);
                }
            }
        } catch (IOException exception) {
            if (!(exception instanceof FileNotFoundException)) {
                LogManager.getLogger().error("Failed to read config!", exception);
            }
        }
    }

    public static void saveToFile() {
        File dir = getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            try (FileWriter fileWriter = new FileWriter(new File(dir, "chatutils.json"))) {
                JsonObject root = new JsonObject();
                JsonObject chatUtils = new JsonObject();
                chatUtils.addProperty(COPY_TO_CLIPBOARD_MESSAGE.name(), COPY_TO_CLIPBOARD_MESSAGE.value());
                chatUtils.addProperty(PREVIEW_CONTENT.name(), PREVIEW_CONTENT.value());
                chatUtils.addProperty(COPY_COLORS.name(), COPY_COLORS.value());
                chatUtils.addProperty(ANTI_SPAM.name(), ANTI_SPAM.value());
                chatUtils.addProperty(TOOLTIP_ENABLED.name(), TOOLTIP_ENABLED.value());
                chatUtils.addProperty(ENABLED.name(), ENABLED.value());
                root.add("ChatUtils", chatUtils);
                gson.toJson(root, fileWriter);
                LogManager.getLogger().info("[chat-utils] Saved settings.");
            } catch (IOException e) {
                LogManager.getLogger().error("[chat-utils] Failed to save settings!", e);
            }
        }
    }

    public static class Value<T> {
        private final String name;
        private T value;
        private final T defaultValue;
        public Value(String name, T value) {
            this.name = name;
            this.value = value;
            this.defaultValue = value;
        }

        public String name() {
            return name;
        }

        public T value() {
            return value;
        }

        public T defaultValue() {
            return defaultValue;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public <V> void read(V element, Function<V, T> elementToValue) {
            try {
                setValue(elementToValue.apply(element));
            } catch (Exception exception) {
                LogManager.getLogger().warn("[chat-utils] Failed to read " + name + " from JSON config (type mismatch)");
            }
        }
    }
}
