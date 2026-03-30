package fr.obeo.koryphaios.server.handler;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.server.workflow.VariableManager;
import fr.obeo.koryphaios.server.workflow.states.WorkflowState;

public interface IEventHandler {

    boolean canHandle(IInput input);

    void handle(IInput input, CollaborationModel model, WorkflowState state, VariableManager variableManager);

}
