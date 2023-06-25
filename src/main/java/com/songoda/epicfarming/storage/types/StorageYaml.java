package com.songoda.epicfarming.storage.types;

import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.storage.Storage;
import com.songoda.epicfarming.storage.StorageItem;
import com.songoda.epicfarming.storage.StorageRow;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StorageYaml extends Storage {
    private final Map<String, Object> toSave = new HashMap<>();
    private Map<String, Object> lastSave = null;

    public StorageYaml(EpicFarming plugin) {
        super(plugin);
    }

    @Override
    public boolean containsGroup(String group) {
        return this.dataFile.contains("data." + group);
    }

    @Override
    public List<StorageRow> getRowsByGroup(String group) {
        List<StorageRow> rows = new ArrayList<>();
        ConfigurationSection currentSection = this.dataFile.getConfigurationSection("data." + group);
        for (String key : currentSection.getKeys(false)) {

            Map<String, StorageItem> items = new HashMap<>();
            ConfigurationSection currentSection2 = this.dataFile.getConfigurationSection("data." + group + "." + key);
            for (String key2 : currentSection2.getKeys(false)) {
                String path = "data." + group + "." + key + "." + key2;
                items.put(key2, new StorageItem(this.dataFile.get(path) instanceof MemorySection
                        ? convertToInLineList(path) : this.dataFile.get(path)));
            }
            if (items.isEmpty()) {
                continue;
            }
            StorageRow row = new StorageRow(key, items);
            rows.add(row);
        }
        return rows;
    }

    private String convertToInLineList(String path) {
        StringBuilder converted = new StringBuilder();
        for (String key : this.dataFile.getConfigurationSection(path).getKeys(false)) {
            converted.append(key).append(":").append(this.dataFile.getInt(path + "." + key)).append(";");
        }
        return converted.toString();
    }

    @Override
    public void prepareSaveItem(String group, StorageItem... items) {
        for (StorageItem item : items) {
            if (item == null || item.asObject() == null) {
                continue;
            }
            this.toSave.put("data." + group + "." + items[0].asString() + "." + item.getKey(), item.asObject());
        }
    }

    @Override
    public void doSave() {
        this.updateData(this.plugin);

        if (this.lastSave == null) {
            this.lastSave = new HashMap<>(this.toSave);
        }

        if (this.toSave.isEmpty()) {
            return;
        }
        Map<String, Object> nextSave = new HashMap<>(this.toSave);

        this.makeBackup();
        this.save();

        this.toSave.clear();
        this.lastSave.clear();
        this.lastSave.putAll(nextSave);
    }

    @Override
    public void save() {
        try {
            for (Map.Entry<String, Object> entry : this.lastSave.entrySet()) {
                if (this.toSave.containsKey(entry.getKey())) {
                    Object newValue = this.toSave.get(entry.getKey());
                    if (!entry.getValue().equals(newValue)) {
                        this.dataFile.set(entry.getKey(), newValue);
                    }
                    this.toSave.remove(entry.getKey());
                } else {
                    this.dataFile.set(entry.getKey(), null);
                }
            }

            for (Map.Entry<String, Object> entry : this.toSave.entrySet()) {
                this.dataFile.set(entry.getKey(), entry.getValue());
            }

            this.dataFile.save();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void makeBackup() {
        File data = new File(this.plugin.getDataFolder(), "data.yml");
        File dataClone = new File(this.plugin.getDataFolder(), "data-backup-" + System.currentTimeMillis() + ".yml");
        try {
            if (data.exists()) {
                copyFile(data, dataClone);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Deque<File> backups = new ArrayDeque<>();
        for (File file : Objects.requireNonNull(this.plugin.getDataFolder().listFiles())) {
            if (file.getName().toLowerCase().contains("data-backup")) {
                backups.add(file);
            }
        }
        if (backups.size() > 3) {
            backups.getFirst().delete();
        }
    }

    @Override
    public void closeConnection() {
        this.dataFile.save();
    }

    private static void copyFile(File source, File dest) throws IOException {
        try (InputStream is = Files.newInputStream(source.toPath());
             OutputStream os = Files.newOutputStream(dest.toPath())) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
