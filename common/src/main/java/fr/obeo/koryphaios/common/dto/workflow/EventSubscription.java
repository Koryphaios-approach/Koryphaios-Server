package fr.obeo.koryphaios.common.dto.workflow;

import java.util.List;

public record EventSubscription(String varName, String eventId, List<Object> args) {
}
