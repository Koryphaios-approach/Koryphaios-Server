package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.dto.TaskEventSubscriptionValue;
import fr.obeo.koryphaios.common.tool.ITaskHandler;
import fr.obeo.koryphaios.common.workflow.Assignment;
import fr.obeo.koryphaios.common.workflow.Expression;
import fr.obeo.koryphaios.common.workflow.Literal;
import fr.obeo.koryphaios.common.workflow.Statement;
import fr.obeo.koryphaios.common.workflow.TaskCall;
import fr.obeo.koryphaios.common.workflow.TaskEventSubscription;
import fr.obeo.koryphaios.common.workflow.VariableReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Interprets and executes workflow task definitions.
 * <p>
 * The TaskInterpreter processes workflow statements and expressions, executing tasks
 * through registered task handlers. It manages:
 * </p>
 * <ul>
 *   <li>Variable assignments and references</li>
 *   <li>Task call interpretation and execution</li>
 *   <li>Expression evaluation</li>
 *   <li>Parameter type validation</li>
 * </ul>
 * <p>
 * Each interpreter instance operates within a variable scope, allowing for isolated
 * execution contexts for different workflow phases or tasks.
 * </p>
 *
 * @see VariableManager
 * @see ITaskHandler
 */
public class TaskInterpreter {

    private static final Logger log = LoggerFactory.getLogger(TaskInterpreter.class);

    private final VariableManager variablesManager;
    private final List<ITaskHandler> taskHandlers;
    private final List<SubscriptionToTaskEvent> subscriptionToTaskEvents = new LinkedList<>();

    /**
     * Constructs a new task interpreter with the specified variable manager and task executors.
     * <p>
     * The interpreter creates a new variable scope as a child of the provided manager,
     * allowing for isolated variable context during task execution.
     * </p>
     *
     * @param variablesManager the parent variable manager for this interpreter
     * @param taskHandlers the list of task executors available for task execution
     */
    public TaskInterpreter(VariableManager variablesManager, List<ITaskHandler> taskHandlers) {
        this.variablesManager = VariableManager.childOf(variablesManager);
        this.taskHandlers = taskHandlers;
    }

    /**
     * Interprets a list of workflow statements.
     * <p>
     * Statements are processed in order, with each statement potentially affecting
     * the variable context for subsequent statements.
     * </p>
     *
     * @param statements the list of statements to interpret
     * @return a list of event initiated by tasks and to listen
     */
    public List<SubscriptionToTaskEvent> interpret(List<Statement> statements) {
        statements.forEach(this::interpretStatement);
        return this.subscriptionToTaskEvents;
    }

    private void interpretStatement(Statement statement) {
        if(statement instanceof Assignment assignment) {
            interpretAssignment(assignment);
        } else if(statement instanceof TaskCall taskCall) {
            interpretTaskCall(taskCall);
        } else if(statement instanceof TaskEventSubscription taskEventSubscription) {
            interpretTaskEventSubscription(taskEventSubscription);
        }
    }

    private void interpretAssignment(Assignment assignment) {
        var result = interpretExpression(assignment.expression());
        if(result instanceof TaskEventSubscriptionValue) {
            log.error("TaskEventSubscriptionValue cannot be assigned to a variable");
        }
        variablesManager.set(assignment.variableName(), result);
    }

    private void interpretTaskEventSubscription(TaskEventSubscription taskEventSubscription) {
        var result = interpretTaskCall(taskEventSubscription.task());

        if(result instanceof TaskEventSubscriptionValue(java.util.UUID eventSubscriptionUuid)) {
            this.subscriptionToTaskEvents.add(new SubscriptionToTaskEvent(taskEventSubscription.eventSubscriptionName(), eventSubscriptionUuid));
        } else {
            log.error("The TaskEventSubscription has not returned a TaskEventSubscriptionValue");
        }
    }

    private Object interpretExpression(Expression expression) {
        if(expression instanceof TaskCall taskCall) {
            return interpretTaskCall(taskCall);
        } else if(expression instanceof VariableReference variableReference) {
            return interpretVariableReference(variableReference);
        } else if(expression instanceof Literal(Object value)) {
            return value;
        }
        return null;
    }

    private Object interpretVariableReference(VariableReference reference) {
        if(this.variablesManager.exist(reference.variableName())) {
            return this.variablesManager.lookup(reference.variableName()).get();
        }
        log.error("Variable '{}' not found.", reference.variableName());
        return null;
    }

    private Object interpretTaskCall(TaskCall taskCall) {
        if(taskCall.args().size() != taskCall.task().parameters().size()) {
            log.error("Task call {} has not the correct number of parameters.", taskCall);
            return null;
        }

        List<Object> values = new ArrayList<>(taskCall.args().size());

        var error = false;
        for(int i = 0; i < taskCall.args().size(); i++) {
            var param = taskCall.task().parameters().get(i);
            var value = interpretExpression(taskCall.args().get(i));
            if(value != null && !param.type().isAssignableFrom(value.getClass())) {
                log.error("Value '{}' for parameter '{}' of '{}' is not of type '{}'.", value, param.name(), taskCall.task().name(), param.type());
                error = true;
            }
            values.add(value);
        }

        if(error) {
            return null;
        }

        var result = taskHandlers.stream()
                .filter(executor -> executor.canHandle(taskCall.task()))
                .findFirst()
                .map(executor -> executor.handle(values.toArray()));
        if(result.isEmpty()) {
            log.error("No task executor was found for '{}'.", taskCall.task().getClass().getSimpleName());
            return null;
        }
        return result.get();
    }

}
