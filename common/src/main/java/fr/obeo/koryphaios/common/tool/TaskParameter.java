package fr.obeo.koryphaios.common.tool;

public record TaskParameter<T>(Class<T> type, String name, String defaultValue) {
}
