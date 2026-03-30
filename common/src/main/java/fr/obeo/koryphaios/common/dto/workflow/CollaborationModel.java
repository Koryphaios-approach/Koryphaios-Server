package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

public record CollaborationModel(String studyId, OrchestrationStrategy orchestration, List<IntegrationStrategy> integrations, List<ContributionStrategy> contributions, List<RequirementMapping> mappings, List<Mock> mocks) {
}
