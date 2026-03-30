package fr.obeo.koryphaios.common.tool;

import fr.obeo.koryphaios.common.adapter.DataResolver;

/**
 * Capability for adapters that can resolve IDs to domain objects.
 * Carries the DataResolver implementation.
 */
public record DataProviderCapability(DataResolver resolver) implements Capability {
}
