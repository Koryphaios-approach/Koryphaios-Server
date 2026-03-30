package fr.obeo.koryphaios.common.dto.workflow;

import fr.obeo.koryphaios.common.tool.EventResult;

import java.util.List;

public record ConditionResult(String condition, EventResult status, List<Statement> statements) {
}
