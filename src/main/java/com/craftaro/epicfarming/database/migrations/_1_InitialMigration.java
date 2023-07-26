package com.craftaro.epicfarming.database.migrations;

import com.craftaro.core.database.DataMigration;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.core.database.MySQLConnector;
import com.craftaro.epicfarming.EpicFarming;

import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {
    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector databaseConnector, String tablePrefix) throws SQLException {
        String autoIncrement = EpicFarming.getPlugin(EpicFarming.class).getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create farms table.
        try (Statement statement = databaseConnector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "active_farms (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
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
        try (Statement statement = databaseConnector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "items (" +
                    "farm_id INTEGER NOT NULL, " +
                    "item VARBINARY(999) NOT NULL" +
                    ")");
        }

        // Create player boosts
        try (Statement statement = databaseConnector.getConnection().createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosted_players (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "multiplier INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }
    }
}
