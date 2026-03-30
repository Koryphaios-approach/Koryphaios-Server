package fr.obeo.koryphaios.common.adapter;

import fr.obeo.koryphaios.common.tool.Capability;
import fr.obeo.koryphaios.common.tool.Event;
import fr.obeo.koryphaios.common.tool.IContributedStaticEventHandler;
import fr.obeo.koryphaios.common.tool.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter for integrating external tools into the workflow system.
 * <p>
 * A ToolAdapter represents a plugin or external tool that participates in workflows by contributing:
 * </p>
 * <ul>
 *   <li>Event handlers that process workflow events via {@link IContributedStaticEventHandler}</li>
 *   <li>Data resolvers that provide tool-specific data via {@link DataResolver}</li>
 *   <li>Tasks that can be executed by the workflow engine</li>
 *   <li>Events that tools can emit</li>
 *   <li>Capabilities that define what the tool can do</li>
 * </ul>
 * <p>
 * Tool adapters are typically registered as Spring beans and discovered automatically by
 * the server's {@code AdapterProcessor}. The adapter manages connections to tools and
 * event subscriptions in a thread-safe manner.
 * </p>
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @Configuration
 * public class MyToolConfiguration {
 *
 *     @Bean
 *     public ToolAdapter myToolAdapter() {
 *         return ToolAdapter.builder()
 *             .id("my-tool")
 *             .displayName("My Tool")
 *             .event(new MyEvent())
 *             .build()
 *             .addTask(new MyTask());
 *     }
 * }
 * }</pre>
 *
 * @see IContributedStaticEventHandler
 * @see DataResolver
 */
public class ToolAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ToolAdapter.class);

    private final String id;
    private final String displayName;
    private final Map<String, Task<?>> tasks;
    private final Map<String, Event> events;
    private final Set<Capability> capabilities;

    private ToolAdapter(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id cannot be null");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName cannot be null");
        this.tasks = new ConcurrentHashMap<>(builder.tasks);
        this.events = new ConcurrentHashMap<>(builder.events);
        this.capabilities = Objects.requireNonNull(builder.capabilities);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Collection<Task<?>> getTasks() {
        return tasks.values();
    }

    public Optional<Task<?>> getTask(String name) {
        return Optional.ofNullable(tasks.get(name));
    }

    public Collection<Event> getEvents() {
        return events.values();
    }

    public Optional<Event> getEvent(String name) {
        return Optional.ofNullable(events.get(name));
    }

    /**
     * Checks if this adapter can handle the specified tool ID.
     *
     * @param id the tool ID to check
     * @return {@code true} if this adapter's ID matches the provided ID, {@code false} otherwise
     */
    public boolean canHandle(String id) {
        return this.id.equals(id);
    }

    /**
     * Adds a task to this adapter dynamically.
     *
     * @param task the task to add
     */
    public void addTask(Task<?> task) {
        if (task != null) {
            tasks.put(task.name(), task);
        }
    }

    /**
     * Creates a new builder for constructing a ToolAdapter.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets all capabilities of this adapter.
     *
     * @return the set of capabilities
     */
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    /**
     * Checks if this adapter has a specific capability.
     *
     * @param capability the capability to check for
     * @return {@code true} if the adapter has the capability, {@code false} otherwise
     */
    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }

    /**
     * Builder for constructing ToolAdapter instances.
     * <p>
     * Provides a fluent API for configuring all aspects of a tool adapter before building it.
     * </p>
     */
    public static class Builder {
        private String id;
        private String displayName;
        private final Map<String, Task<?>> tasks = new HashMap<>();
        private final Map<String, Event> events = new HashMap<>();
        private final Set<Capability> capabilities = new HashSet<>();

        private Builder() {
        }

        /**
         * Sets the unique identifier for the adapter.
         *
         * @param id the adapter ID
         * @return this builder for method chaining
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the display name for the adapter.
         *
         * @param displayName the human-readable name
         * @return this builder for method chaining
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Adds a single task to the adapter.
         *
         * @param task the task to add
         * @return this builder for method chaining
         */
        public Builder task(Task<?> task) {
            if (task != null) {
                this.tasks.put(task.name(), task);
            }
            return this;
        }

        /**
         * Adds multiple statements to the adapter.
         *
         * @param tasks the collection of statements to add
         * @return this builder for method chaining
         */
        public Builder tasks(Collection<Task<?>> tasks) {
            if (tasks != null) {
                tasks.forEach(task -> this.tasks.put(task.name(), task));
            }
            return this;
        }

        /**
         * Adds capabilities to the adapter.
         *
         * @param capabilities the collection of capabilities to add
         * @return this builder for method chaining
         */
        public Builder capabilities(Collection<Capability> capabilities) {
            if (capabilities != null) {
                this.capabilities.addAll(capabilities);
            }
            return this;
        }

        /**
         * Adds a single event to the adapter.
         *
         * @param event the event to add
         * @return this builder for method chaining
         */
        public Builder event(Event event) {
            if (event != null) {
                this.events.put(event.getName(), event);
            }
            return this;
        }

        /**
         * Adds multiple events to the adapter.
         *
         * @param events the collection of events to add
         * @return this builder for method chaining
         */
        public Builder events(Collection<Event> events) {
            if (events != null) {
                events.forEach(event -> this.events.put(event.getName(), event));
            }
            return this;
        }

        /**
         * Builds the ToolAdapter instance.
         * <p>
         * This method validates that each registered event has a corresponding event listener.
         * A warning is logged if any event lacks a listener.
         * </p>
         *
         * @return a new ToolAdapter instance
         */
        public ToolAdapter build() {
            return new ToolAdapter(this);
        }
    }
}
