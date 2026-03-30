package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.workflow.CollaborationModel;
import fr.obeo.koryphaios.server.handler.IEventHandler;

import org.eclipse.sirius.koryphaios.data.Requirement;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Factory responsible for creating {@link WorkflowProcessor} instances.
 * <p>
 * This isolates the construction logic and dependency wiring from the
 * {@link WorkflowProcessorRegistry}, which is only concerned with storage and lookup.
 * </p>
 *
 * @see WorkflowProcessor
 * @see WorkflowProcessorRegistry
 */
@Service
public class WorkflowProcessorFactory {

    private final List<IEventHandler> eventHandlers;
    private final ContributedEventDispatcher contributedEventDispatcher;

    public WorkflowProcessorFactory(List<IEventHandler> eventHandlers,
                                    ContributedEventDispatcher contributedEventDispatcher) {
        this.eventHandlers = eventHandlers;
        this.contributedEventDispatcher = contributedEventDispatcher;
    }

    /**
     * Creates a new workflow processor for the given study and collaboration model.
     *
     * @param studyId the unique study identifier
     * @param model   the collaboration model defining the workflow structure
     * @param requirements the list of requirements to consider in this study
     * @return a new {@link WorkflowProcessor} instance
     */
    public WorkflowProcessor create(String studyId, CollaborationModel model, List<Requirement> requirements) {
        return new WorkflowProcessor(studyId, model, eventHandlers, contributedEventDispatcher, requirements);
    }
}
