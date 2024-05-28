package com.craftaro.epicfarming.boost;

import com.craftaro.core.database.Data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BoostData implements Data {
    private final int multiplier;
    private final long endTime;
    private final UUID player;

    // Default constructor for deserialization
    public BoostData() {
        this.multiplier = 0;
        this.endTime = 0;
        this.player = null;
    }

    public BoostData(int multiplier, long endTime, UUID player) {
        this.multiplier = multiplier;
        this.endTime = endTime;
        this.player = player;
    }

    public int getMultiplier() {
        return this.multiplier;
    }

    public UUID getPlayer() {
        return this.player;
    }

    public long getEndTime() {
        return this.endTime;
    }

    @Override
    public int hashCode() {
        int result = 31 * this.multiplier;

        result = 31 * result + (this.player == null ? 0 : this.player.hashCode());
        result = 31 * result + (int) (this.endTime ^ (this.endTime >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BoostData)) {
            return false;
        }

        BoostData other = (BoostData) obj;
        return this.multiplier == other.multiplier && this.endTime == other.endTime
                && Objects.equals(this.player, other.player);
    }

    @Override
    public UUID getUniqueId() {
        return this.player;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("player", this.player.toString());
        map.put("multiplier", this.multiplier);
        map.put("end_time", this.endTime);
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        return new BoostData((int) map.get("multiplier"), (long) map.get("end_time"), UUID.fromString((String) map.get("player")));
    }

    @Override
    public String getTableName() {
        return "boosted_players";
    }
}
