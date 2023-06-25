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

}
