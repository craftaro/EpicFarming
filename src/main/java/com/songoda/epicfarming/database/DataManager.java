package com.songoda.epicfarming.database;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTCore;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.boost.BoostData;
import com.songoda.epicfarming.farming.Farm;
import com.songoda.epicfarming.farming.FarmType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector connector, Plugin plugin) {
        super(connector, plugin);
    }

    public void createBoost(BoostData boostData) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createBoostedPlayer = "INSERT INTO " + this.getTablePrefix() + "boosted_players (player, multiplier, end_time) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createBoostedPlayer)) {
                statement.setString(1, boostData.getPlayer().toString());
                statement.setInt(2, boostData.getMultiplier());
                statement.setLong(3, boostData.getEndTime());
                statement.executeUpdate();
            }
        }));
    }

    public void getBoosts(Consumer<List<BoostData>> callback) {
        List<BoostData> boosts = new ArrayList<>();
        this.async(() -> this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                String selectBoostedPlayers = "SELECT * FROM " + this.getTablePrefix() + "boosted_players";
                ResultSet result = statement.executeQuery(selectBoostedPlayers);
                while (result.next()) {
                    UUID player = UUID.fromString(result.getString("player"));
                    int multiplier = result.getInt("multiplier");
                    long endTime = result.getLong("end_time");
                    boosts.add(new BoostData(multiplier, endTime, player));
                }
            }

            this.sync(() -> callback.accept(boosts));
        }));
    }

    public void deleteBoost(BoostData boostData) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosted_players WHERE end_time = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteBoost)) {
                statement.setLong(1, boostData.getEndTime());
                statement.executeUpdate();
            }
        }));
    }

    public void createFarm(Farm farm) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {
            String createFarm = "INSERT INTO " + this.getTablePrefix() + "active_farms (farm_type, level, placed_by, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createFarm)) {
                statement.setString(1, farm.getFarmType().name());
                statement.setInt(2, farm.getLevel().getLevel());
                statement.setString(3, farm.getPlacedBy().toString());
                statement.setString(4, farm.getLocation().getWorld().getName());
                statement.setInt(5, farm.getLocation().getBlockX());
                statement.setInt(6, farm.getLocation().getBlockY());
                statement.setInt(7, farm.getLocation().getBlockZ());

                statement.executeUpdate();
            }

            int farmId = this.lastInsertedId(connection, "active_farms");
            farm.setId(farmId);

            String createItem = "INSERT INTO " + this.getTablePrefix() + "items (farm_id, item) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createItem)) {
                for (ItemStack item : farm.getItems()) {
                    statement.setInt(1, farm.getId());

                    try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(stream)) {
                        bukkitStream.writeObject(item);
                        statement.setString(2, Base64.getEncoder().encodeToString(stream.toByteArray()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }), "create");
    }

    public void createFarms(List<Farm> farms) {
        for (Farm farm : farms) {
            createFarm(farm);
        }
    }

    public void updateFarm(Farm farm) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateFarm = "UPDATE " + this.getTablePrefix() + "active_farms SET level = ?, farm_type = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateFarm)) {
                statement.setInt(1, farm.getLevel().getLevel());
                statement.setString(2, farm.getFarmType().name());
                statement.setInt(3, farm.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteFarm(Farm farm) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteFarm = "DELETE FROM " + this.getTablePrefix() + "active_farms WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteFarm)) {
                statement.setInt(1, farm.getId());
                statement.executeUpdate();
            }

            String deleteItems = "DELETE FROM " + this.getTablePrefix() + "items WHERE farm_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteItems)) {
                statement.setInt(1, farm.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void updateItemsAsync(Farm farm) {
        this.async(() -> updateItems(farm));
    }

    public void updateItems(Farm farm) {
        this.databaseConnector.connect(connection -> {
            String deleteItems = "DELETE FROM " + this.getTablePrefix() + "items WHERE farm_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteItems)) {
                statement.setInt(1, farm.getId());
                statement.executeUpdate();
            }

            String createItem = "INSERT INTO " + this.getTablePrefix() + "items (farm_id, item) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createItem)) {
                for (ItemStack item : farm.getItems()) {
                    statement.setInt(1, farm.getId());

                    try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(stream)) {
                        bukkitStream.writeObject(item);
                        statement.setString(2, Base64.getEncoder().encodeToString(stream.toByteArray()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
    }

    public void getFarms(Consumer<Map<Integer, Farm>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            Map<Integer, Farm> farms = new HashMap<>();
            try (Statement statement = connection.createStatement()) {
                String selectFarms = "SELECT * FROM " + this.getTablePrefix() + "active_farms";
                ResultSet result = statement.executeQuery(selectFarms);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));

                    if (world == null) {
                        continue;
                    }

                    int id = result.getInt("id");
                    int level = result.getInt("level");

                    String placedByStr = result.getString("placed_by");
                    UUID placedBy = placedByStr == null ? null : UUID.fromString(result.getString("placed_by"));

                    FarmType farmType = FarmType.valueOf(result.getString("farm_type"));

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

                    Farm farm = new Farm(location, EpicFarming.getInstance().getLevelManager().getLevel(level), placedBy);
                    farm.setId(id);
                    farm.setFarmType(farmType);

                    farms.put(id, farm);
                }
            }

            try (Statement statement = connection.createStatement()) {
                String selectItems = "SELECT * FROM " + this.getTablePrefix() + "items";
                ResultSet result = statement.executeQuery(selectItems);
                while (result.next()) {
                    int id = result.getInt("farm_id");

                    ItemStack item = null;
                    try (BukkitObjectInputStream stream = new BukkitObjectInputStream(
                            new ByteArrayInputStream(Base64.getDecoder().decode(result.getString("item"))))) {
                        item = (ItemStack) stream.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Farm farm = farms.get(id);
                    if (farm == null) {
                        break;
                    }

                    if (item != null) {
                        farm.addItem(item);
                    }
                }
            }
            this.sync(() -> callback.accept(farms));
        }));
    }
}