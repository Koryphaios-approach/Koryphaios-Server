package fr.obeo.koryphaios.common.workflow;

public record TaskEventSubscription(String eventSubscriptionName, TaskCall task) implements Statement {
}
