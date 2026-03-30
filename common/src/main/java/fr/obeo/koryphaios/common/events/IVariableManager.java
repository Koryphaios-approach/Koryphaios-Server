package fr.obeo.koryphaios.common.events;

import java.util.Optional;

/**
 * Interface for managing workflow variables with hierarchical scoping.
 * <p>
 * The variable manager provides a scoped context for workflow execution, allowing:
 * </p>
 * <ul>
 *   <li>Hierarchical variable scopes with parent-child relationships</li>
 *   <li>Variable lookup with automatic fallback to parent scopes</li>
 *   <li>Isolated variable spaces for different workflow phases and tasks</li>
 * </ul>
 * <p>
 * Variables are searched in the current scope first, then recursively in parent scopes
 * until found or the root scope is reached.
 * </p>
 * <p>
 * The server module provides a concrete implementation of this interface with factory methods
 * for creating root managers, child scopes, and merged managers.
 * </p>
 */
public interface IVariableManager {

    /**
     * Retrieves a variable by name from the current scope or parent scopes.
     * <p>
     * The lookup starts in the current scope and recursively searches parent scopes
     * until the variable is found or the root scope is reached.
     * </p>
     *
     * @param variableName the name of the variable to retrieve
     * @return an Optional containing the variable value if found, empty otherwise
     */
    Optional<Object> lookup(String variableName);

    /**
     * Adds or updates a variable in the current scope.
     * <p>
     * The variable is stored in the current scope only and does not affect parent scopes.
     * If a variable with the same name exists in a parent scope, it will be shadowed.
     * </p>
     *
     * @param variableName the name of the variable to add or update
     * @param variableValue the value to store
     */
    void set(String variableName, Object variableValue);

    /**
     * Checks if a variable is defined in the current scope or any parent scope.
     *
     * @param variableName the name of the variable to check
     * @return {@code true} if the variable exists in the scope hierarchy, {@code false} otherwise
     */
    boolean exist(String variableName);

    Optional<IVariableManager> unStack();

}
