package fr.obeo.koryphaios.common.workflow;

import fr.obeo.koryphaios.common.dto.workflow.CollaborationModel;

import java.util.Optional;

/**
 * Interface for converting workflow models between DTO and domain representations.
 * <p>
 * The workflow converter transforms GraphQL input DTOs into domain model objects by:
 * </p>
 * <ul>
 *   <li>Resolving string IDs to domain objects via data resolvers</li>
 *   <li>Validating that all referenced objects exist</li>
 *   <li>Building the complete domain model graph</li>
 *   <li>Handling conversion failures gracefully</li>
 * </ul>
 * <p>
 * This conversion is necessary because GraphQL inputs use string IDs for references,
 * while the domain model uses actual object references.
 * </p>
 *
 */
public interface IWorkflowConverter {

    /**
     * Converts a DTO collaboration model to a domain collaboration model.
     * <p>
     * This method resolves all ID references in the DTO to their corresponding domain objects.
     * If any ID cannot be resolved, the conversion fails and an empty Optional is returned.
     * </p>
     *
     * @param dtoModel the DTO collaboration model containing string ID references
     * @return an Optional containing the domain CollaborationModel if conversion succeeded, empty otherwise
     */
    Optional<fr.obeo.koryphaios.common.workflow.CollaborationModel> convert(
            CollaborationModel dtoModel);
}
