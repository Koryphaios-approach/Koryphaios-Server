package fr.obeo.koryphaios.common.tool;

public interface ITaskHandler {

    boolean canHandle(Task<?> task);

    Object handle(Object... params);
}
