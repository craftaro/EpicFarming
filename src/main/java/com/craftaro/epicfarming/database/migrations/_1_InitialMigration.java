package com.craftaro.epicfarming.database.migrations;

import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.database.DataMigration;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.database.MySQLConnector;
import com.craftaro.epicfarming.EpicFarming;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {
    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {

        // Create farms table.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "active_farms (" +
                    "id INTEGER PRIMARY KEY, " +
                    "farm_type TEXT NOT NULL, " +
                    "level INTEGER NOT NULL, " +
                    "placed_by VARCHAR(36), " +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL " +
                    ")");
        }

        // Create items.
        // Items are stored as Base64. Dunno if this is the most efficient way to
        // store them, though.
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "items (" +
                    "farm_id INTEGER NOT NULL, " +
                    "item VARBINARY(999) NOT NULL" +
                    ")");
        }

        // Create player boosts
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "boosted_players (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "multiplier INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }
    }
}
