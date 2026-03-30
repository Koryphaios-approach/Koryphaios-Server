package fr.obeo.koryphaios.server.workflow;

import java.util.UUID;

public record SubscriptionToTaskEvent(String name, UUID subscriptionId) {
}
