package fr.obeo.koryphaios.common.dto.workflow;

import fr.obeo.koryphaios.common.workflow.ModelPhase;

public record ModelDependency(ModelPhase model, ModelPhase dependency) {
}
