package de.fabiansiemens.hyperion.persistence.roll;

import de.fabiansiemens.hyperion.core.features.roll.IgnoreReason;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.DiceEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiceEntryEmbeddable {

    @Column(nullable = false)
    private int sides;

    @Column(name = "dice_value", nullable = false)
    private int value;

    @Column(nullable = false)
    private boolean ignored;

    /** Grund der Verwerfung – null wenn {@code ignored = false}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "ignore_reason", length = 16)
    private IgnoreReason ignoreReason;

    static DiceEntryEmbeddable of(DiceEntry entry) {
        DiceEntryEmbeddable e = new DiceEntryEmbeddable();
        e.sides        = entry.getSides();
        e.value        = entry.getValue();
        e.ignored      = entry.isIgnored();
        e.ignoreReason = entry.getIgnoreReason();
        return e;
    }
}
