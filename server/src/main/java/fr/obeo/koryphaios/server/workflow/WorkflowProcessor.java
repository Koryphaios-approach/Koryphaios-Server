package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.dto.IContributedEventInput;
import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.common.workflow.ModelDependency;
import fr.obeo.koryphaios.common.workflow.ModelState;
import fr.obeo.koryphaios.server.handler.IEventHandler;
import fr.obeo.koryphaios.server.workflow.states.ModelFlowState;
import fr.obeo.koryphaios.server.workflow.states.WorkflowState;

import org.eclipse.sirius.koryphaios.data.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Main workflow processing engine that manages workflow execution lifecycle.
 * <p>
 * The WorkflowProcessor is a per-study instance that holds the workflow state and
 * routes incoming events to the appropriate handler:
 * </p>
 * <ul>
 *   <li><strong>Standard events</strong> — routed to {@link IEventHandler} implementations</li>
 *   <li><strong>Contributed events</strong> — delegated to the {@link ContributedEventDispatcher}</li>
 * </ul>
 *
 * @see WorkflowState
 * @see IEventHandler
 * @see ContributedEventDispatcher
 */
public class WorkflowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    private final String studyId;
    private final CollaborationModel model;
    private final WorkflowState workflowState;
    private final List<IEventHandler> eventHandlers;
    private final ContributedEventDispatcher contributedEventDispatcher;
    private final List<Requirement> requirements;
    private final VariableManager defaultRootVariableManager;

    /**
     * Constructs a new workflow processor for the specified study and collaboration model.
     *
     * @param studyId                    the unique identifier for the study
     * @param model                      the collaboration model defining the workflow structure
     * @param eventHandlers              the list of standard event handlers
     * @param contributedEventDispatcher the dispatcher for contributed events from tools
     * @param requirements the list of requirements to consider in this study
     */
    WorkflowProcessor(String studyId,
                      CollaborationModel model,
                      List<IEventHandler> eventHandlers,
                      ContributedEventDispatcher contributedEventDispatcher,
                      List<Requirement> requirements) {
        this.studyId = studyId;
        this.model = model;
        this.eventHandlers = eventHandlers;
        this.contributedEventDispatcher = contributedEventDispatcher;
        this.requirements = requirements;
        this.defaultRootVariableManager = VariableManager.root();
        this.requirements.forEach(req -> defaultRootVariableManager.set(req.getName(), req));
        defaultRootVariableManager.set("reqMapping", this.model.mappings());;

        List<ModelFlowState> initialStates = model.modelInterfaces().stream()
                .map(mi -> new ModelFlowState(mi, ModelState.NONE, null))
                .toList();
        this.workflowState = new WorkflowState(initialStates);
    }

    /**
     * Checks if this processor can handle the given input.
     *
     * @param input the input to check
     * @return {@code true} if the input's study ID matches this processor's
     */
    public boolean canHandle(IInput input) {
        return input.studyId().equals(this.studyId);
    }

    /**
     * Handles an input event by routing it to the appropriate handler.
     *
     * @param input the input event to handle
     */
    public void handleInput(IInput input) {
        if (input instanceof IContributedEventInput contributedInput) {
            handleContributedEvent(contributedInput);
        } else {
            handleStandardEvent(input);
        }
    }

    /**
     * Gets the model dependencies defined in the workflow's orchestration.
     *
     * @return the list of model dependencies
     */
    public List<ModelDependency> getModelDependencies() {
        return model.orchestration().modelDependencies();
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    // ──────────────────────────── Internal ────────────────────────────

    private void handleContributedEvent(IContributedEventInput input) {
        var modelState = findModelState(input.modelName());
        if (modelState == null || modelState.phaseState().isEmpty()) {
            logger.error("Contributed event for model '{}' has no active model/phase state", input.modelName());
            return;
        }
        contributedEventDispatcher.dispatch(input, modelState, this.defaultRootVariableManager);
    }

    private void handleStandardEvent(IInput input) {
        eventHandlers.stream()
                .filter(handler -> handler.canHandle(input))
                .findFirst()
                .ifPresent(handler -> {
                    var variableManager = VariableManager.root();
                    this.requirements.forEach(req -> variableManager.set(req.getName(), req));
                    variableManager.set("reqMapping", this.model.mappings());
                    handler.handle(input, model, workflowState, VariableManager.childOf(variableManager));
                });
    }

    private @Nullable ModelFlowState findModelState(String modelName) {
        return workflowState.modelStates().stream()
                .filter(state -> state.modelInterface().name().equals(modelName))
                .findFirst()
                .orElse(null);
    }
}
