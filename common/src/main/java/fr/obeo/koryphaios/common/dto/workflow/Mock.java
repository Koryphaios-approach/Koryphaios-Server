package fr.obeo.koryphaios.common.dto.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.sirius.koryphaios.data.impl.VersionImpl;

import java.util.Objects;

public class Mock extends VersionImpl implements ModelInterface {

    private final String name;

    @JsonCreator
    public Mock(@JsonProperty("name") String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String name() {
        return this.getName();
    }
}
