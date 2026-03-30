package fr.obeo.koryphaios.common.tool;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.obeo.koryphaios.common.dto.EventResultInput;

/**
 * Interface representing the result of an event contribution by a tool adapter.
 * <p>
 * This interface encapsulates the outcome of event processing, including the acceptation status
 * and an optional message providing additional context about the result.
 * </p>
 *
 * @see AcceptationStatus
 */
@JsonDeserialize(as = EventResultInput.class)
public interface EventResult {

    /**
     * Gets the message providing details about the processing result.
     *
     * @return a message describing the result, may be null
     */
    String getMessage();

    /**
     * Gets the acceptation status indicating how the event was processed.
     *
     * @return the acceptation status
     */
    AcceptationStatus getResult();

}
