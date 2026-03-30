package fr.obeo.koryphaios.common.dto.workflow;

import fr.obeo.koryphaios.common.workflow.ModelDependency;

import java.util.List;

public record OrchestrationStrategy(List<ModelDependency> modelDependencies) {
}
