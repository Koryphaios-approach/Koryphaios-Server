package fr.obeo.koryphaios.common.tool;

import fr.obeo.koryphaios.common.dto.IInput;

public interface IContributedEventHandler {

    /**
     * Checks if this handler can process the given input event.
     * <p>
     * This method should perform a quick type check or validation to determine
     * if the handler is applicable for the event. The workflow engine will only
     * </p>
     *
     * @param input the input event to check
     * @return {@code true} if this handler can process the event, {@code false} otherwise
     */
    boolean canHandle(IInput input);

}
