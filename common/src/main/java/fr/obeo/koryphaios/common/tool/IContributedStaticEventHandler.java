package fr.obeo.koryphaios.common.tool;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.events.IVariableManager;
import fr.obeo.koryphaios.common.workflow.EventSubscription;

/**
 * Interface for event handlers contributed by tool adapters to participate in workflows.
 * <p>
 * Tool adapters implement this interface to process workflow events and contribute to
 * workflow decisions. Each handler can:
 * </p>
 * <ul>
 *   <li>Check if it can handle a specific event type</li>
 *   <li>Process the event with access to workflow variables</li>
 *   <li>Return an acceptation status indicating the processing result</li>
 * </ul>
 * <p>
 * The workflow engine invokes all registered handlers for each event, collecting their
 * acceptation statuses to determine the overall workflow progression.
 * </p>
 *
 * @see AcceptationStatus
 * @see IVariableManager
 */
public interface IContributedStaticEventHandler extends IContributedEventHandler {

    /**
     * Processes the input event with access to workflow variables.
     * <p>
     * This method is called by the workflow engine when an event occurs that this
     * handler can process. The handler can:
     * </p>
     * <ul>
     *   <li>Read workflow variables using the variable manager</li>
     *   <li>Validate the event based on tool-specific logic</li>
     *   <li>Store results in the variable manager for use by other handlers or tasks</li>
     *   <li>Return an acceptation status indicating the result</li>
     * </ul>
     *
     * @param input the input event to process
     * @param subscription the event subscription parametrized by the workflow specified
     * @param variableManager the variable manager for accessing and storing workflow context
     * @return the acceptation status indicating how the event was processed, IGNORED if the subscription params does not match the input
     */
    AcceptationStatus handle(IInput input, EventSubscription subscription, IVariableManager variableManager);

}
