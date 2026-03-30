package fr.obeo.koryphaios.server.workflow.states;

import fr.obeo.koryphaios.common.workflow.ModelInterface;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the complete state of a workflow execution across all model interfaces.
 * <p>
 * The WorkflowState maintains a map of model interfaces to their corresponding flow states,
 * tracking the current execution state for each model participating in the workflow. This
 * provides a centralized view of the entire workflow progression and enables state management
 * operations such as updating model states and querying current workflow status.
 * </p>
 * <p>
 * This class is used by the WorkflowProcessor to maintain workflow state throughout execution.
 * </p>
 *
 * @see ModelFlowState
 */
public class WorkflowState {
    private final Map<ModelInterface, ModelFlowState> modelStates;

    /**
     * Constructs a new workflow state from a list of model flow states.
     * <p>
     * The constructor converts the list into a map indexed by model interface for
     * efficient lookup and update operations during workflow execution.
     * </p>
     *
     * @param modelStates the initial list of model flow states for this workflow
     */
    public WorkflowState(List<ModelFlowState> modelStates) {
        this.modelStates = modelStates.stream()
                .collect(Collectors.toMap(ModelFlowState::modelInterface, state -> state));
    }

    /**
     * Returns the current list of all model flow states in this workflow.
     * <p>
     * This method provides a snapshot of all model states at the time of the call.
     * The returned list is a new instance and modifications to it will not affect
     * the internal state.
     * </p>
     *
     * @return an immutable list of all model flow states in this workflow
     */
    public List<ModelFlowState> modelStates() {
        return modelStates.values().stream().toList();
    }

    /**
     * Updates or adds a model flow state in this workflow.
     * <p>
     * If a state for the model interface already exists, it will be replaced with
     * the new state. This method is used to track workflow progression as models
     * transition through different phases and states.
     * </p>
     *
     * @param modelState the model flow state to add or update
     */
    public void pushModelState(ModelFlowState modelState) {
        this.modelStates.put(modelState.modelInterface(), modelState);
    }
}
