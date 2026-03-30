package fr.obeo.koryphaios.common.workflow;

import java.util.List;

public record OrchestrationStrategy(List<ModelDependency> modelDependencies) {
}
