package fr.obeo.koryphaios.server;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.server.IServer;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.server.workflow.WorkflowProcessorRegistry;

import java.util.List;

import org.eclipse.sirius.koryphaios.data.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Main workflow orchestration engine implementing the {@link IServer} interface.
 * <p>
 * The server coordinates workflow execution by:
 * </p>
 * <ul>
 *   <li>Managing workflow processors through the {@link WorkflowProcessorRegistry}</li>
 *   <li>Dispatching input events to appropriate workflow processors</li>
 *   <li>Starting new workflow instances from collaboration models</li>
 * </ul>
 * <p>
 * This service is the central entry point for all workflow operations and is typically
 * injected into tool adapters and event handlers that need to interact with the workflow system.
 * </p>
 *
 * @see IServer
 * @see WorkflowProcessorRegistry
 */
@Service
public class Server implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final WorkflowProcessorRegistry registry;

    public Server(WorkflowProcessorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void runWorkflow(String studyId, CollaborationModel collaborationModel, List<Requirement> requirements) {
        logger.info("Starting workflow execution for study: {}", collaborationModel.study().getName());
        registry.getOrCreateWorkflowProcessor(studyId, collaborationModel, requirements);
    }

    @Override
    public void dispatch(IInput input) {
        registry.get(input.studyId())
                .filter(processor -> processor.canHandle(input))
                .ifPresent(workflow -> workflow.handleInput(input));
    }
}
