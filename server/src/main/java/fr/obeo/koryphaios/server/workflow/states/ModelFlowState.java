package fr.obeo.koryphaios.server.workflow.states;

import fr.obeo.koryphaios.common.workflow.ModelInterface;
import fr.obeo.koryphaios.common.workflow.ModelState;

import java.util.Optional;

public class ModelFlowState {
    private ModelInterface modelInterface;
    private ModelState globalVersionState;
    private WorkflowPhaseState phaseState;

    public ModelFlowState(ModelInterface modelInterface, ModelState globalVersionState, WorkflowPhaseState phaseState) {
        this.modelInterface = modelInterface;
        this.globalVersionState = globalVersionState;
        this.phaseState = phaseState;
    }

    public ModelInterface modelInterface() {
        return modelInterface;
    }

    public void setModelInterface(ModelInterface modelInterface) {
        this.modelInterface = modelInterface;
    }

    public ModelState globalVersionState() {
        return globalVersionState;
    }

    public void setGlobalVersionState(ModelState globalVersionState) {
        this.globalVersionState = globalVersionState;
    }

    public Optional<WorkflowPhaseState> phaseState() {
        return Optional.ofNullable(phaseState);
    }

    public void setPhaseState(WorkflowPhaseState phaseState) {
        this.phaseState = phaseState;
    }
}
