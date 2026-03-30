package fr.obeo.koryphaios.common.adapter;

import java.util.Optional;

/**
 * Interface for resolving data queries from workflow tasks.
 * <p>
 * Data resolvers provide a bridge between the workflow engine and tool-specific APIs,
 * allowing workflows to query dynamic data during execution. Implementations can:
 * </p>
 * <ul>
 *   <li>Query tool-specific data (models, users, configurations)</li>
 *   <li>Transform IDs or queries into domain objects</li>
 *   <li>Provide context-aware data based on workflow variables</li>
 * </ul>
 * <p>
 * This interface is typically implemented by tool adapters to provide access to
 * their specific data sources.
 * </p>
 */
public interface DataResolver {

    /**
     * Checks if this resolver can resolve an object of the specified type for the given ID.
     *
     * @param id the ID of the object to check
     * @param clazz the class type of the object
     * @param <T> the type of the object
     * @return {@code true} if this resolver can resolve the object, {@code false} otherwise
     */
    <T> boolean canResolveObject(String id, Class<T> clazz);

    /**
     * Resolves an ID to a domain object of the specified type.
     * <p>
     * This method is called by the workflow engine when a task needs to access
     * tool-specific data. The resolver should query its underlying data source
     * and return the requested object if found.
     * </p>
     *
     * @param id the ID of the object to resolve
     * @param clazz the class type of the object to resolve
     * @param <T> the type of the object
     * @return an Optional containing the resolved object if found, empty otherwise
     */
    <T> Optional<T> resolveObject(String id, Class<T> clazz);
}
