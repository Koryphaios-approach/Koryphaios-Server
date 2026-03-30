package fr.obeo.koryphaios.common.workflow;

import org.eclipse.sirius.koryphaios.data.impl.VersionImpl;

import java.util.Objects;

public class Mock extends VersionImpl implements ModelInterface {

    private final String name;

    public Mock(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public String name() {
        return this.getName();
    }
}
