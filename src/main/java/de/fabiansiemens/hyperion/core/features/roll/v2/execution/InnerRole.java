package de.fabiansiemens.hyperion.core.features.roll.v2.execution;

/** Welche Rolle ein {@link NodeKind#INNER}-Knoten in der äußeren Operation spielte. */
public enum InnerRole {
    COUNT,
    SIDES,
    MODIFIER,
    REROLL_THRESHOLD,
    CHECK_THRESHOLD,
    NONE
}
