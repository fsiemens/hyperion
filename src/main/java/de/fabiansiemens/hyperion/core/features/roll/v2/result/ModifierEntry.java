package de.fabiansiemens.hyperion.core.features.roll.v2.result;

import lombok.Value;

/**
 * Ein einzelner Modifikator-Eintrag, wie er vom Parser erzeugt wurde.
 *
 * <p>Jeder {@code +N}, {@code -N}, {@code ++N} bzw. {@code --N} im Würfelausdruck
 * erzeugt einen eigenen Eintrag – sie werden nicht vorab summiert.
 * Das erlaubt eine vollständige Auflistung in der Detail-Auswertung.
 *
 * @param signedValue  Vorzeichenbehafteter Wert des Modifikators (z. B. +5, -3)
 * @param appliedToAll {@code true} → per-Wurf ({@code ++}/{}--{@code}/{}all{});
 *                     {@code false} → einmalig auf Gesamtsumme
 */
@Value
public class ModifierEntry {

    int     signedValue;
    boolean appliedToAll;

    @Override
    public String toString() {
        String sign = signedValue >= 0 ? "+" : "−";
        return sign + Math.abs(signedValue);
    }
}
