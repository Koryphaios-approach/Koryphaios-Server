package fr.obeo.koryphaios.common.workflow;

import fr.obeo.koryphaios.common.tool.EventResult;

import java.util.List;

public record EventResultMatcher(String eventSubscriptionVar, EventResult status, List<Statement> statements) {
}
