package fr.obeo.koryphaios.common.workflow;

import org.eclipse.sirius.koryphaios.data.Study;

import java.util.List;

public record CollaborationModel(Study study, OrchestrationStrategy orchestration, List<IntegrationStrategy> integrations, List<ContributionStrategy> contributions, List<RequirementMapping> mappings, List<ModelInterface> modelInterfaces) {
}
