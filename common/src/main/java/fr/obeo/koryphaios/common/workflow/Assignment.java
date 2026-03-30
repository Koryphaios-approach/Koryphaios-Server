package fr.obeo.koryphaios.common.workflow;

public record Assignment(String variableName, Expression expression) implements Statement {

}
