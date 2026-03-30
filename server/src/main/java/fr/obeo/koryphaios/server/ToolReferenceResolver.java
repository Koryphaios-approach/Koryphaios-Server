package fr.obeo.koryphaios.server;

import fr.obeo.koryphaios.common.adapter.ToolAdapter;
import fr.obeo.koryphaios.common.tool.DataProviderCapability;
import fr.obeo.koryphaios.common.tool.Event;
import fr.obeo.koryphaios.common.tool.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves qualified tool references (format {@code "toolId#name"}) to domain objects.
 * <p>
 * This service centralises the parsing and resolution of qualified identifiers that follow
 * the {@code "toolId#name"} convention used throughout the workflow definition DTOs.
 * It delegates adapter lookups to the {@link AdapterProcessor} and handles:
 * </p>
 * <ul>
 *   <li>Event resolution — {@link #resolveEvent(String)}</li>
 *   <li>Task resolution — {@link #resolveTask(String)}</li>
 *   <li>Arbitrary data resolution via {@link DataProviderCapability} — {@link #resolveData(String, Class)}</li>
 * </ul>
 *
 * @see AdapterProcessor
 */
@Service
public class ToolReferenceResolver {

    private static final Logger logger = LoggerFactory.getLogger(ToolReferenceResolver.class);
    private final AdapterProcessor adapterProcessor;

    public ToolReferenceResolver(AdapterProcessor adapterProcessor) {
        this.adapterProcessor = adapterProcessor;
    }

    /**
     * Resolves a qualified event identifier to an {@link Event} object.
     *
     * @param qualifiedId the identifier in the format {@code "toolId#eventName"}
     * @return an Optional containing the resolved event, or empty if not found or malformed
     */
    public Optional<Event> resolveEvent(String qualifiedId) {
        return parseQualifiedId(qualifiedId).flatMap(ref ->
                adapterProcessor.findAdapter(ref.toolId())
                        .map(ToolAdapter::getEvents)
                        .flatMap(events -> events.stream()
                                .filter(event -> event.getName().equals(ref.name()))
                                .findFirst()));
    }

    /**
     * Resolves a qualified task identifier to a {@link Task} object.
     *
     * @param qualifiedId the identifier in the format {@code "toolId#taskName"}
     * @return an Optional containing the resolved task, or empty if not found or malformed
     */
    public Optional<Task<?>> resolveTask(String qualifiedId) {
        return parseQualifiedId(qualifiedId).flatMap(ref ->
                adapterProcessor.findAdapter(ref.toolId())
                        .flatMap(adapter -> adapter.getTask(ref.name())));
    }

    /**
     * Resolves data by delegating to {@link DataProviderCapability} resolvers across all adapters.
     *
     * @param id    the identifier to resolve
     * @param clazz the expected type of the resolved object
     * @param <T>   the expected type
     * @return an Optional containing the resolved object, or empty if not found
     */
    public <T> Optional<T> resolveData(String id, Class<T> clazz) {
        return adapterProcessor.getAdapters().stream()
                .flatMap(adapter -> adapter.getCapabilities().stream())
                .filter(DataProviderCapability.class::isInstance)
                .map(cap -> ((DataProviderCapability) cap).resolver())
                .filter(resolver -> resolver.canResolveObject(id, clazz))
                .findFirst()
                .flatMap(resolver -> resolver.resolveObject(id, clazz));
    }

    // ────────────────────────────── Internal ──────────────────────────────

    private Optional<QualifiedRef> parseQualifiedId(String qualifiedId) {
        if (qualifiedId == null) {
            logger.error("Qualified ID is null");
            return Optional.empty();
        }
        var parts = qualifiedId.split("#");
        if (parts.length != 2) {
            logger.error("Invalid qualified ID '{}' — expected format 'toolId#name'", qualifiedId);
            return Optional.empty();
        }
        return Optional.of(new QualifiedRef(parts[0], parts[1]));
    }

    private record QualifiedRef(String toolId, String name) {}
}
