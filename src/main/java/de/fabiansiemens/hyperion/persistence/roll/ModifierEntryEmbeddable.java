package de.fabiansiemens.hyperion.persistence.roll;

import de.fabiansiemens.hyperion.core.features.roll.v2.result.ModifierEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModifierEntryEmbeddable {

    /** Vorzeichenbehafteter Wert, z. B. +5 oder -3. */
    @Column(name = "signed_value", nullable = false)
    private int signedValue;

    /** {@code true} = per-Wurf (++/--), {@code false} = einmalig auf Summe. */
    @Column(name = "applied_to_all", nullable = false)
    private boolean appliedToAll;

    static ModifierEntryEmbeddable of(ModifierEntry entry) {
        ModifierEntryEmbeddable e = new ModifierEntryEmbeddable();
        e.signedValue  = entry.getSignedValue();
        e.appliedToAll = entry.isAppliedToAll();
        return e;
    }
}
