package de.fabiansiemens.hyperion.persistence.roll;

import de.fabiansiemens.hyperion.core.features.roll.v2.result.CheckEntry;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckEntryEmbeddable {

    /** Prüfart: check / beat / nail / limit / cut. */
    @Column(nullable = false, length = 8)
    private String kind;

    @Column(nullable = false)
    private int threshold;

    /** Tatsächlich geprüfter Wert (Würfelergebnis + per-roll Modifier). */
    @Column(nullable = false)
    private int against;

    @Column(nullable = false)
    private boolean passed;

    static CheckEntryEmbeddable of(CheckEntry entry) {
        CheckEntryEmbeddable e = new CheckEntryEmbeddable();
        e.kind      = entry.getKind();
        e.threshold = entry.getThreshold();
        e.against   = entry.getAgainst();
        e.passed    = entry.isPassed();
        return e;
    }
}
