package fr.obeo.koryphaios.common.server;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;

import java.util.List;

import org.eclipse.sirius.koryphaios.data.Requirement;

/**
 * Interface for server operations that can be called by adapters.
 * This allows adapters to request workflow execution from the server.
 */
public interface IServer {

    /**
     * Run a workflow by processing the domain collaboration model.
     *
     * @param studyId the identifier of the study
     * @param collaborationModel the domain collaboration model (already converted from DTO)
     * @param requirements the list of requirements to consider in this study
     */
    void runWorkflow(String studyId, CollaborationModel collaborationModel, List<Requirement> requirements);

    /**
     * Dispatch an input to the correct running workflow.
     *
     * @param input the input to run
     */
    void dispatch(IInput input);
}
