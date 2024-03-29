package com.craftaro.epicfarming.utils;

import com.craftaro.third_party.org.jooq.Query;
import com.craftaro.third_party.org.jooq.impl.DSL;
import com.craftaro.core.utils.ItemSerializer;
import com.craftaro.epicfarming.EpicFarming;
import com.craftaro.epicfarming.farming.Farm;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    private static EpicFarming plugin;

    public static void init(EpicFarming plugin) {
        DataHelper.plugin = plugin;
    }

    public static void createFarms(List<Farm> farms) {

        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            List<Query> queries = new ArrayList<>();
            for (Farm farm : farms) {
                queries.add(dslContext.insertInto(DSL.table(plugin.getDataManager().getTablePrefix() + "active_farms"))
                        .columns(DSL.field("farm_type"),
                                DSL.field("level"),
                                DSL.field("placed_by"),
                                DSL.field("world"),
                                DSL.field("x"),
                                DSL.field("y"),
                                DSL.field("z"))
                        .values(farm.getFarmType().name(),
                                farm.getLevel().getLevel(),
                                farm.getPlacedBy().toString(),
                                farm.getLocation().getWorld().getName(),
                                farm.getLocation().getBlockX(),
                                farm.getLocation().getBlockY(),
                                farm.getLocation().getBlockZ()));
            }
            dslContext.batch(queries).execute();
        });
    }

    public static void updateItemsAsync(Farm farm) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> updateItems(farm));
    }

    public static void updateItems(Farm farm) {
        String tablePrefix = plugin.getDataManager().getTablePrefix();

        plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
            dslContext.deleteFrom(DSL.table(tablePrefix + "items"))
                    .where(DSL.field("farm_id").eq(farm.getId()))
                    .execute();

            List<Query> queries = new ArrayList<>();
            for (int i = 0; i < farm.getItems().size(); i++) {
                queries.add(dslContext.insertInto(DSL.table(tablePrefix + "items"))
                        .columns(DSL.field("farm_id"), DSL.field("item"))
                        .values(farm.getId(), ItemSerializer.serializeItem(farm.getItems().get(i))));
            }
            dslContext.batch(queries).execute();
        });
    }
}
