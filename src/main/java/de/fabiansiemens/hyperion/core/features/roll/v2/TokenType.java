package de.fabiansiemens.hyperion.core.features.roll.v2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Alle Token-Typen der Würfel-Grammatik.
 */
@Getter
@RequiredArgsConstructor
public enum TokenType {

    // Kern
    NUMBER  ("ganze Zahl"),
    D       ("Würfelindikator ('d')"),

    // Arithmetische Operatoren
    PLUS        ("'+'"),
    MINUS       ("'-'"),
    PLUS_PLUS   ("'++'"),
    MINUS_MINUS ("'--'"),

    // Struktur
    COMMA    ("Verknüpfungsoperator (',')"),   // trennt eigenständige Würfeloperationen
    QUESTION ("Verkettung ('?')"),
    COLON    ("Oder (':')"),
    LPAREN   ("Linke Klammer ('(')"),
    RPAREN   ("Rechte Klammer (')')"),

    // Würfel-Argumente
    ADV    ("Vorteilsindikator ('adv')"),
    DISADV ("Nachteilsindikator ('disadv')"),
    RR     ("Reroll ('rr')"),
    CAP_MIN("untere Begrenzung ('min')"),
    CAP_MAX("obere Begrenzung ('max')"),

    // Vergleichsoperatoren
    CMP_LT  ("kleiner als ('<')"),
    CMP_LTE ("kleiner/gleich ('<=')"),
    CMP_EQ  ("gleich ('=' / '==')"),
    CMP_GTE ("größer/gleich ('>=')"),
    CMP_GT  ("größer als ('>')"),
    CMP_MOD ("Modulo ('%')"),

    // Prüf-Argumente
    CHECK("Prüf-Argument ('check')"),
    BEAT ("Prüf-Argument ('beat')"),
    NAIL ("Prüf-Argument ('nail')"),
    LIMIT("Prüf-Argument ('limit')"),
    CUT  ("Prüf-Argument ('cut')"),

    // Scope-Suffix
    ALL("All-Suffix ('all')"),

    EOF("Ende");

    private final String label;
}
