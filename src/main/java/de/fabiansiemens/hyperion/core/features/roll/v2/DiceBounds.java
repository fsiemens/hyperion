package de.fabiansiemens.hyperion.core.features.roll.v2;

/**
 * Konfigurierbare Grenzen für Würfelanzahl und Seitenanzahl.
 *
 * <p>Im Spring-Kontext wird diese Klasse als {@code @ConfigurationProperties}-Bean
 * bereitgestellt und via {@code application.properties} befüllt:
 *
 * <pre>
 * dice.count.min=0
 * dice.count.max=100
 * dice.sides.min=2
 * dice.sides.max=1000
 * </pre>
 *
 * <p>Außerhalb von Spring steht {@link #DEFAULT} als unveränderliche Instanz
 * mit sinnvollen Standardwerten bereit.
 *
 * @param countMin Minimale Würfelanzahl (≥ 0)
 * @param countMax Maximale Würfelanzahl
 * @param sidesMin Minimale Seitenanzahl (≥ 2)
 * @param sidesMax Maximale Seitenanzahl
 */
public record DiceBounds(int countMin, int countMax, int sidesMin, int sidesMax) {

    public static final DiceBounds DEFAULT = new DiceBounds(0, 100, 2, 1000);

    /**
     * Prüft, ob {@code count} im erlaubten Bereich liegt.
     *
     * @param count    zu prüfende Anzahl
     * @param position Zeichenposition im Eingabe-String (für Fehlermeldung)
     * @throws DiceParseException wenn der Wert außerhalb des Bereichs liegt
     */
    public void validateCount(int count, int position) {
        if (count < countMin || count > countMax) {
            throw new DiceParseException(String.format(
                "Würfelanzahl %d liegt außerhalb des erlaubten Bereichs [%d, %d] an Position %d.",
                count, countMin, countMax, position));
        }
    }

    /**
     * Prüft, ob {@code sides} im erlaubten Bereich liegt.
     *
     * @param sides    zu prüfende Seitenanzahl
     * @param position Zeichenposition im Eingabe-String (für Fehlermeldung)
     * @throws DiceParseException wenn der Wert außerhalb des Bereichs liegt
     */
    public void validateSides(int sides, int position) {
        if (sides < sidesMin || sides > sidesMax) {
            throw new DiceParseException(String.format(
                "Seitenanzahl %d liegt außerhalb des erlaubten Bereichs [%d, %d] an Position %d.",
                sides, sidesMin, sidesMax, position));
        }
    }
}
