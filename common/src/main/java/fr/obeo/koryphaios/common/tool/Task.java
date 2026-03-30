package fr.obeo.koryphaios.common.tool;

import java.util.List;

public abstract class Task<T> {
    public abstract String name();
    public abstract List<TaskParameter<?>> parameters();

    public abstract Class<T> returnType();
}
