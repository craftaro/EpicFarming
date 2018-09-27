package com.songoda.epicfarming.boost;

import java.util.Objects;
import java.util.UUID;

public class BoostData {

    private final int multiplier;
    private final long endTime;
    private final UUID player;

    public BoostData(int multiplier, long endTime, UUID player) {
        this.multiplier = multiplier;
        this.endTime = endTime;
        this.player = player;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public UUID getPlayer() {
        return player;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public int hashCode() {
        int result = 31 * multiplier;

        result = 31 * result + (this.player == null ? 0 : player.hashCode());
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BoostData)) return false;

        BoostData other = (BoostData) obj;
        return multiplier == other.multiplier && endTime == other.endTime
                && Objects.equals(player, other.player);
    }

}
