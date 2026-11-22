package de.fabiansiemens.hyperion.persistence.roll;

import de.fabiansiemens.hyperion.core.features.roll.v2.result.CritEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CritEntryEmbeddable {

    @Column(name = "dice_value", nullable = false)
    private int diceValue;

    /** {@code true} = kritischer Erfolg, {@code false} = kritisches Scheitern. */
    @Column(name = "crit_success", nullable = false)
    private boolean critSuccess;

    static CritEntryEmbeddable of(CritEntry entry) {
        CritEntryEmbeddable e = new CritEntryEmbeddable();
        e.diceValue   = entry.getDiceValue();
        e.critSuccess = entry.isCritSuccess();
        return e;
    }
}
