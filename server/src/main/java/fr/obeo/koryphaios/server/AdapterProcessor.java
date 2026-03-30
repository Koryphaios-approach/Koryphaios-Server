package fr.obeo.koryphaios.server;

import fr.obeo.koryphaios.common.adapter.ToolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Registry for all {@link ToolAdapter} instances in the system.
 * <p>
 * Tool adapters are automatically discovered as Spring beans during application startup.
 * This service provides adapter lookup and per-adapter querying capabilities (events,
 * tasks). For cross-adapter resolution of qualified identifiers ({@code "toolId#name"}),
 * see {@link ToolReferenceResolver}.
 * </p>
 *
 * @see ToolAdapter
 * @see ToolReferenceResolver
 */
@Service
public class AdapterProcessor {

    private final Logger logger = LoggerFactory.getLogger(AdapterProcessor.class);
    private final List<ToolAdapter> adapters;

    /**
     * Constructs an AdapterProcessor with the given list of tool adapters.
     * <p>
     * Tool adapters are automatically injected by Spring from all registered beans.
     * </p>
     *
     * @param adapters the list of tool adapters to manage
     */
    public AdapterProcessor(List<ToolAdapter> adapters) {
        this.adapters = adapters;
        adapters.forEach(adapter -> logger.info("Adapter '{}' registered.", adapter.getId()));
    }

    /**
     * Returns all registered adapters.
     *
     * @return an unmodifiable view of the adapter list
     */
    public List<ToolAdapter> getAdapters() {
        return adapters;
    }

    /**
     * Finds an adapter by its tool ID.
     *
     * @param toolId the unique tool identifier
     * @return an Optional containing the adapter if found
     */
    public Optional<ToolAdapter> findAdapter(String toolId) {
        return adapters.stream()
                .filter(adapter -> adapter.getId().equals(toolId))
                .findFirst();
    }

}
