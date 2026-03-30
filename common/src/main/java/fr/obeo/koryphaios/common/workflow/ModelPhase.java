package fr.obeo.koryphaios.common.workflow;

import org.eclipse.sirius.koryphaios.data.Version;

public record ModelPhase(ModelState state, Version modelVersion) {
}
