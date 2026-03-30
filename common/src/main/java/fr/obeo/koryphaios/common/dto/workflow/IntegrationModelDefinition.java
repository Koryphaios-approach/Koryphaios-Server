package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

public record IntegrationModelDefinition(String name, List<Mock> integratedModels) implements ModelInterface {
}
