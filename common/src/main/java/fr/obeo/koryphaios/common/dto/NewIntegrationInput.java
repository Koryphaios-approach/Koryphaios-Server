package fr.obeo.koryphaios.common.dto;

import java.util.List;

public record NewIntegrationInput(String studyId, String modelName, List<String> integratedModelsName) implements IInput {
}
