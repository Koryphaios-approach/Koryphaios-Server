package fr.obeo.koryphaios.common.workflow;

import fr.obeo.koryphaios.common.tool.Event;

import java.util.List;

public record EventSubscription(String varName, Event event, List<Object> args) {
}
