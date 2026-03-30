package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.dto.IContributedEventInput;
import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.tool.AcceptationStatus;
import fr.obeo.koryphaios.common.tool.Event;
import fr.obeo.koryphaios.common.tool.IContributedDynamicEventHandler;
import fr.obeo.koryphaios.common.tool.IContributedEventHandler;
import fr.obeo.koryphaios.common.tool.IContributedStaticEventHandler;
import fr.obeo.koryphaios.common.workflow.ContributionStrategy;
import fr.obeo.koryphaios.common.workflow.ModelState;
import fr.obeo.koryphaios.server.ToolReferenceResolver;
import fr.obeo.koryphaios.server.workflow.states.ModelFlowState;
import fr.obeo.koryphaios.server.workflow.states.WorkflowPhaseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Orchestrates the processing of contributed events from tool adapters.
 * <p>
 * This service encapsulates the full lifecycle of a contributed event:
 * </p>
 * <ol>
 *   <li>Resolving the event identifier to a domain {@link Event}</li>
 *   <li>Finding the matching {@link IContributedStaticEventHandler}</li>
 *   <li>Iterating over event subscriptions and invoking the handler</li>
 *   <li>Triggering task execution via the {@link WorkflowTaskExecutor}</li>
 *   <li>Validating and advancing the workflow phase if all preconditions are met</li>
 * </ol>
 *
 * @see IContributedStaticEventHandler
 * @see WorkflowTaskExecutor
 * @see ToolReferenceResolver
 */
@Service
public class ContributedEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ContributedEventDispatcher.class);

    Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final ToolReferenceResolver referenceResolver;
    private final List<IContributedEventHandler> contributedEventHandlers;
    private final WorkflowTaskExecutor taskExecutor;

    public ContributedEventDispatcher(ToolReferenceResolver referenceResolver,
                                      List<IContributedEventHandler> contributedEventHandlers,
                                      WorkflowTaskExecutor taskExecutor) {
        this.referenceResolver = referenceResolver;
        this.contributedEventHandlers = contributedEventHandlers;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Dispatches a contributed event input against the given model flow state.
     * <p>
     * The caller is responsible for ensuring that the model flow state has an active phase state.
     * </p>
     *
     * @param input      the contributed event input from a tool adapter
     * @param modelState the model flow state that owns the active workflow phase
     */
    public void dispatch(IContributedEventInput input, ModelFlowState modelState, VariableManager rootVariableManager) {
        var phaseState = modelState.phaseState().orElse(null);
        if (phaseState == null) {
            logger.error("Model '{}' does not have an active phase state", input.modelName());
            return;
        }

        var handler = findHandler(input);
        if (handler == null) {
            logger.error("No contributed event handler found for '{}'", input.getClass().getSimpleName());
            return;
        }

        if (UUID_REGEX.matcher(input.eventId()).matches()) {
            if (handler instanceof IContributedDynamicEventHandler dynamicEventHandler) {
                processDynamicSubscriptions(input, modelState, phaseState, dynamicEventHandler, UUID.fromString(input.eventId()), rootVariableManager);
            } else {
                logger.error("A dynamic event must be handled by a IContributedDynamicEventHandler");
                return;
            }
        }

        var event = referenceResolver.resolveEvent(input.eventId()).orElse(null);
        if (event == null) {
            logger.error("Event not found for id '{}'", input.eventId());
            return;
        }

        if (handler instanceof IContributedStaticEventHandler staticEventHandler) {
            processStaticSubscriptions(input, modelState, phaseState, staticEventHandler, event);
        } else {
            logger.error("A static event must be handled by a IContributedStaticEventHandler");
        }
    }

    // ─────────────────────────── Event processing ───────────────────────────

    private void processStaticSubscriptions(IContributedEventInput input,
                                      ModelFlowState modelState,
                                      WorkflowPhaseState phaseState,
                                      IContributedStaticEventHandler handler,
                                      Event event) {
        phaseState.strategy().eventSubscriptions().stream()
                .filter(subscription -> subscription.event() == event)
                .forEach(subscription -> {
                    var variableManager = VariableManager.childOf(
                            phaseState.getVariableManagers().get(subscription.varName()));
                    phaseState.getVariableManagers().put(subscription.varName(), variableManager);

                    var result = handler.handle(input, subscription, variableManager);
                    if (result != AcceptationStatus.IGNORED) {
                        phaseState.setPreconditionResult(subscription.varName(), result);
                        taskExecutor.executeTasks(
                                subscription.varName(),
                                phaseState,
                                phaseState.strategy().onEventSubscription(),
                                variableManager);
                        validateAndAdvance(modelState);
                    }

                    variableManager.unStack().ifPresent(
                            parent -> phaseState.getVariableManagers().put(subscription.varName(), parent));
                });
    }

    private void processDynamicSubscriptions(IContributedEventInput input,
            ModelFlowState modelState,
            WorkflowPhaseState phaseState,
            IContributedDynamicEventHandler handler,
            UUID eventId,
            VariableManager rootVariableManager) {
        var dynamicEventSubscription = phaseState.getSubscribedEventNameFromUuid(eventId);

        if(dynamicEventSubscription.isPresent()) {
            var subscriptionName = dynamicEventSubscription.get();
            var variableManager = VariableManager.childOf(rootVariableManager);

            var result = handler.handle(input, variableManager);
            if (result != AcceptationStatus.IGNORED) {
                taskExecutor.executeTasks(
                        subscriptionName,
                        phaseState,
                        phaseState.strategy().onEventSubscription(),
                        variableManager);
                validateAndAdvance(modelState);
            }
        }
    }

    // ────────────────────────── Phase validation ─────────────────────────

    /**
     * Validates the current phase and, if all preconditions are accepted,
     * advances the model flow state and clears the phase.
     */
    private void validateAndAdvance(ModelFlowState modelFlowState) {
        modelFlowState.phaseState()
                .filter(this::isFullyAccepted)
                .ifPresent(state -> {
                    if (state.strategy() instanceof ContributionStrategy) {
                        modelFlowState.setGlobalVersionState(ModelState.CONTRIBUTED);
                    } else {
                        modelFlowState.setGlobalVersionState(ModelState.INTEGRATED);
                    }
                    modelFlowState.setPhaseState(null);
                });
    }

    private boolean isFullyAccepted(WorkflowPhaseState state) {
        return state.getPreconditionResults().values().stream()
                .allMatch(status -> status == AcceptationStatus.ACCEPTED);
    }

    // ──────────────────────── Handler resolution ────────────────────────

    private @Nullable IContributedEventHandler findHandler(IInput input) {
        return contributedEventHandlers.stream()
                .filter(handler -> handler.canHandle(input))
                .findFirst()
                .orElse(null);
    }
}
