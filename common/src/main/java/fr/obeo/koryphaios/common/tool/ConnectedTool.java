package fr.obeo.koryphaios.common.tool;

import fr.obeo.koryphaios.common.IPayload;
import reactor.core.publisher.Sinks;

import java.util.List;

public class ConnectedTool {
    private final String id;
    private final List<String> users;
    private final Sinks.Many<IPayload> payloadSink;

    public ConnectedTool(String id, List<String> users) {
        this.id = id;
        this.users = users;
        this.payloadSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public String id() {
        return id;
    }

    public List<String> users() {
        return users;
    }

    public void sendPayload(IPayload payload) {
        payloadSink.tryEmitNext(payload);
    }

    public Sinks.Many<IPayload> getPayloadSink() {
        return payloadSink;
    }
}
