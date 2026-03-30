package fr.obeo.koryphaios.common.workflow;

import fr.obeo.koryphaios.common.tool.Task;

import java.util.List;

public record TaskCall(Task<?> task, List<Expression> args) implements Expression, Statement {
}
