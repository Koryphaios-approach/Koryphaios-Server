package fr.obeo.koryphaios.common.tool;

/**
 * Sealed interface representing capabilities that a ToolAdapter can provide.
 * Each capability type can carry its own implementation.
 */
public sealed interface Capability permits DataProviderCapability {
}
