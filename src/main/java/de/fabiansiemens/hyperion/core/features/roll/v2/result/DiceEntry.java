package de.fabiansiemens.hyperion.core.features.roll.v2.result;

import de.fabiansiemens.hyperion.core.features.roll.IgnoreReason;
import lombok.Value;

/** Einzelner Würfelwurf inklusive Verwerfungsgrund. */
@Value
public class DiceEntry {

    int          sides;
    int          value;
    boolean      ignored;
    IgnoreReason ignoreReason;

    public static DiceEntry active(int sides, int value) {
        return new DiceEntry(sides, value, false, null);
    }

    public static DiceEntry ignored(int sides, int value, IgnoreReason reason) {
        return new DiceEntry(sides, value, true, reason);
    }

    @Override
    public String toString() {
        String sign = value >= 0 ? "+" : "−";
        return sign + Math.abs(value);
    }
}
