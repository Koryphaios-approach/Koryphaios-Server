package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.events.IVariableManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IVariableManager} providing hierarchical variable scoping for workflows.
 * <p>
 * Variables are stored in the local scope and lookups automatically fall back to parent scopes
 * when a variable is not found locally. Factory methods enforce the intended usage patterns:
 * </p>
 * <ul>
 *   <li>{@link #root()} — creates an empty root scope (no parent)</li>
 *   <li>{@link #childOf(IVariableManager)} — creates an empty child scope that inherits from a parent</li>
 *   <li>{@link #from(IVariableManager, IVariableManager)} — copies local variables from a source scope
 *       into a new scope parented to another</li>
 *   <li>{@link #merge(List)} / {@link #merge(List, IVariableManager)} — flattens local variables from
 *       multiple scopes into a single new scope</li>
 * </ul>
 *
 * @see IVariableManager
 */
public class VariableManager implements IVariableManager {

    private final IVariableManager parent;
    private final Map<String, Object> variables = new HashMap<>();

    private VariableManager(Map<String, Object> initialVariables, IVariableManager parent) {
        this.variables.putAll(initialVariables);
        this.parent = parent;
    }

    // ──────────────────────────── Factory methods ────────────────────────────

    /**
     * Creates an empty root variable manager with no parent scope.
     *
     * @return a new root scope
     */
    public static VariableManager root() {
        return new VariableManager(Map.of(), null);
    }

    /**
     * Creates an empty child scope whose variable lookups fall back to {@code parent}.
     *
     * @param parent the parent scope (must not be {@code null})
     * @return a new child scope
     */
    public static VariableManager childOf(IVariableManager parent) {
        Objects.requireNonNull(parent, "parent must not be null");
        return new VariableManager(Map.of(), parent);
    }

    /**
     * Creates a new scope initialised with the <em>local</em> variables of {@code source}
     * and parented to {@code parent}.
     * <p>
     * Only the direct variables of {@code source} are copied; variables inherited from
     * {@code source}'s own parent chain are <strong>not</strong> included.
     * </p>
     *
     * @param source the scope whose local variables are copied
     * @param parent the parent scope for the new manager (may be {@code null} for a root scope)
     * @return a new scope with copied variables
     */
    public static VariableManager from(IVariableManager source, IVariableManager parent) {
        Objects.requireNonNull(source, "source must not be null");
        if (source instanceof VariableManager vm) {
            return new VariableManager(vm.variables, parent);
        }
        return new VariableManager(Map.of(), parent);
    }

    /**
     * Creates a new scope having the same parent as the one given in parameters.
     *
     * @param source the source manager
     * @return a new scope
     */
    public static VariableManager from(IVariableManager source) {
        Objects.requireNonNull(source, "source must not be null");
        return source.unStack()
                .map(VariableManager::childOf)
                .orElseGet(VariableManager::root);
    }

    /**
     * Merges the <em>local</em> variables of several managers into a single root scope.
     * <p>
     * When the same key appears in multiple managers the <strong>first occurrence</strong> wins.
     * </p>
     *
     * @param managers the managers to merge
     * @return a new root scope containing all merged variables
     */
    public static VariableManager merge(List<? extends IVariableManager> managers) {
        return merge(managers, null);
    }

    /**
     * Merges the <em>local</em> variables of several managers into a single scope
     * parented to {@code parent}.
     * <p>
     * When the same key appears in multiple managers the <strong>first occurrence</strong> wins.
     * Only managers that are instances of {@link VariableManager} contribute their local variables.
     * </p>
     *
     * @param managers the managers to merge
     * @param parent   the parent scope for the result (may be {@code null})
     * @return a new scope containing all merged variables
     */
    public static VariableManager merge(List<? extends IVariableManager> managers, IVariableManager parent) {
        var merged = managers.stream()
                .filter(VariableManager.class::isInstance)
                .map(VariableManager.class::cast)
                .map(m -> m.variables)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
        return new VariableManager(merged, parent);
    }

    // ──────────────────────── IVariableManager contract ──────────────────────

    @Override
    public Optional<Object> lookup(String variableName) {
        return Optional.ofNullable(variables.get(variableName))
                .or(() -> parent != null ? parent.lookup(variableName) : Optional.empty());
    }

    @Override
    public void set(String variableName, Object variableValue) {
        variables.put(variableName, variableValue);
    }

    @Override
    public boolean exist(String variableName) {
        return variables.containsKey(variableName)
                || (parent != null && parent.exist(variableName));
    }

    @Override
    public Optional<IVariableManager> unStack() {
        return Optional.ofNullable(parent);
    }
}
