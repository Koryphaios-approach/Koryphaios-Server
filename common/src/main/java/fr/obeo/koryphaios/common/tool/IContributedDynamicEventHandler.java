package fr.obeo.koryphaios.common.tool;

import fr.obeo.koryphaios.common.dto.IInput;
import fr.obeo.koryphaios.common.events.IVariableManager;

/**
 * A dynamic event is an event subscribed through a task
 *
 */
public interface IContributedDynamicEventHandler extends IContributedEventHandler {

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
     * @param variableManager the variable manager for accessing and storing workflow context
     * @return the acceptation status indicating how the event was processed, IGNORED if the subscription params does not match the input
     */
    AcceptationStatus handle(IInput input, IVariableManager variableManager);

}
