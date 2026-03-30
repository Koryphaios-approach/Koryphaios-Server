package fr.obeo.koryphaios.server.workflow;

import fr.obeo.koryphaios.common.workflow.CollaborationModel;

import org.eclipse.sirius.koryphaios.data.Requirement;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing {@link WorkflowProcessor} instances associated with studies.
 * <p>
 * This service maintains a thread-safe map of processors (one per study) and
 * delegates processor creation to the {@link WorkflowProcessorFactory}.
 * </p>
 *
 * @see WorkflowProcessorFactory
 * @see WorkflowProcessor
 */
@Service
public class WorkflowProcessorRegistry {

    private final Map<String, WorkflowProcessor> processors = new ConcurrentHashMap<>();
    private final WorkflowProcessorFactory factory;

    public WorkflowProcessorRegistry(WorkflowProcessorFactory factory) {
        this.factory = factory;
    }

    /**
     * Retrieves an existing workflow processor for the given study, or creates a new one.
     *
     * @param studyId the unique identifier of the study
     * @param model   the collaboration model to use for workflow processing
     * @param requirements the list of requirements to consider in this study
     * @return the workflow processor associated with the study
     */
    public WorkflowProcessor getOrCreateWorkflowProcessor(String studyId, CollaborationModel model, List<Requirement> requirements) {
        return processors.computeIfAbsent(studyId, id -> factory.create(id, model, requirements));
    }

    /**
     * Retrieves an existing workflow processor for the given study.
     *
     * @param studyId the unique identifier of the study
     * @return the workflow processor if it exists
     */
    public Optional<WorkflowProcessor> get(String studyId) {
        return Optional.ofNullable(processors.get(studyId));
    }
}
