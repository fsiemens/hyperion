package de.fabiansiemens.hyperion.core.features.roll.v2.result;

import lombok.Value;

/** Kritischer Erfolg oder kritisches Scheitern eines Würfelwurfs. */
@Value
public class CritEntry {
    int     diceValue;
    boolean critSuccess;
}
