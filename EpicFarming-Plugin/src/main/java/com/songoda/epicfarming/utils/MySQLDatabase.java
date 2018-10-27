package com.songoda.epicfarming.utils;


import com.songoda.epicfarming.EpicFarmingPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {

    private final EpicFarmingPlugin instance;

    private Connection connection;

    public MySQLDatabase(EpicFarmingPlugin instance) {
        this.instance = instance;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + instance.getConfig().getString("Database.IP") + ":" + instance.getConfig().getString("Database.Port") + "/" + instance.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, instance.getConfig().getString("Database.Username"), instance.getConfig().getString("Database.Password"));

            //ToDo: This is sloppy
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "farms` (\n" +
                    "\t`location` TEXT NULL,\n" +
                    "\t`level` INT NULL,\n" +
                    "\t`placedby` TEXT NULL,\n" +
                    "\t`contents` TEXT NULL, \n"+
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + instance.getConfig().getString("Database.Prefix") + "boosts` (\n" +
                    "\t`endtime` TEXT NULL,\n" +
                    "\t`amount` INT NULL,\n" +
                    "\t`player` TEXT NULL\n" +
                    ")");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}