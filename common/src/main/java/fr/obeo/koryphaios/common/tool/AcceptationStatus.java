package fr.obeo.koryphaios.common.tool;

/**
 * Enumeration representing the result of event processing by a contributed event handler.
 * <p>
 * Tool adapters return this status to indicate how they processed a workflow event.
 * The workflow engine uses these statuses to determine overall workflow progression.
 * </p>
 */
public enum AcceptationStatus {
    /**
     * The event was successfully processed and accepted.
     * <p>
     * This indicates that the handler validated the event and approved the workflow action.
     * </p>
     */
    ACCEPTED,

    /**
     * The event was rejected due to validation failure or business logic constraints.
     * <p>
     * This indicates that the handler found issues with the event that prevent the
     * workflow from progressing.
     * </p>
     */
    REJECTED,

    /**
     * The event was conditionally accepted with constraints or warnings.
     * <p>
     * This indicates that the handler accepted the event but flagged it for review
     * or imposed conditions on further processing.
     * </p>
     */
    GUARDED,

    /**
     * The event was not applicable to this handler.
     * <p>
     * This indicates that the handler does not have an opinion on this event and
     * defers to other handlers for decision-making.
     * </p>
     */
    IGNORED
}
