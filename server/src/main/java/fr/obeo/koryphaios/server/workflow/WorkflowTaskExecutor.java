package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.tool.ITaskHandler;
import fr.obeo.koryphaios.common.workflow.EventResultMatcher;
import fr.obeo.koryphaios.server.workflow.states.WorkflowPhaseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Executes workflow tasks based on event subscription results and phase state.
 * <p>
 * This service is responsible for a single concern: evaluating {@link EventResultMatcher}
 * conditions against the current phase state and executing their associated statements
 * through the {@link TaskInterpreter}.
 * </p>
 *
 * @see TaskInterpreter
 * @see WorkflowPhaseState
 */
@Service
public class WorkflowTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTaskExecutor.class);

    private final List<ITaskHandler> handlers;

    /**
     * Constructs a new workflow task executor with the specified task handlers.
     *
     * @param handlers the list of task handlers available for task execution
     */
    public WorkflowTaskExecutor(List<ITaskHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * Executes workflow tasks for conditions that match the updated subscription state.
     * <p>
     * This method evaluates all conditions and executes their associated statements when:
     * </p>
     * <ul>
     *   <li>The subscription var is a wildcard ({@code "*"}) and all preconditions match</li>
     *   <li>The subscription var matches the updated one and the expected status matches</li>
     * </ul>
     *
     * @param updatedSubscriptionVar the name of the subscription variable that was updated
     * @param phaseState             the current workflow phase state
     * @param conditions             the list of event result matchers to evaluate
     * @param variables              the parent variable manager for task execution
     */
    public void executeTasks(String updatedSubscriptionVar,
                             WorkflowPhaseState phaseState,
                             List<EventResultMatcher> conditions,
                             VariableManager variables) {

        var newState = phaseState.getPreconditionResults().get(updatedSubscriptionVar);

        for (var condition : conditions) {
            if (condition.eventSubscriptionVar().equals("*")
                    && phaseState.getPreconditionResults().values().stream()
                    .allMatch(st -> st == condition.status().getResult())) {

                log.info("Execute all statements of '*'");
                var newScope = VariableManager.merge(
                        List.copyOf(phaseState.getVariableManagers().values()), variables);
                new TaskInterpreter(newScope, handlers).interpret(condition.statements())
                        .forEach(phaseState::subscribeToTaskEvent);

            } else if (condition.eventSubscriptionVar().equals(updatedSubscriptionVar)
                    && condition.status().getResult().equals(newState)) {

                log.info("Execute all statements of the eventSubscriptionVar");
                var newScope = VariableManager.from(
                        phaseState.getVariableManagers().get(condition.eventSubscriptionVar()), variables);
                new TaskInterpreter(newScope, handlers).interpret(condition.statements())
                        .forEach(phaseState::subscribeToTaskEvent);
            }
        }
    }
}
