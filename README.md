# Koryphaios Workflow Collaboration System

A comprehensive workflow collaboration system for heterogeneous fields such as model-based complex systems engineering.

## Project Structure

This is a Maven monorepo containing two main modules:

```
Koryphaios/
├── pom.xml                    # Root Maven configuration
├── README.md                  # This file
├── common/                    # Shared library
│   ├── pom.xml
│   └── src/main/java/fr/obeo/ocp/koryphaios/common/
│       ├── adapter/           # Tool adapter framework
│       ├── dto/               # Data Transfer Objects
│       ├── events/            # Event system interfaces
│       ├── server/            # Server interfaces
│       ├── tool/              # Tool and event handling
│       └── workflow/          # Workflow model definitions
└── server/                    # GraphQL server & workflow engine
    ├── pom.xml
    └── src/main/java/fr/obeo/ocp/koryphaios/server/
        ├── Server.java        # Main server class
        ├── AdapterProcessor.java
        ├── handler/           # Event handlers
        └── workflow/          # Workflow processing engine
```

## Workflow System Overview

The Koryphaios system provides a flexible workflow collaboration framework with:

1. **Contribution Strategy**: Defines conditions for validating contributions (similar to pull requests)
2. **Integration Strategy**: Specifies conditions for integration of models
3. **Event-Driven Architecture**: Tools contribute events that drive workflow progression

## Quick Start

### Prerequisites
- Java 25
- Maven 3.6+

### Build All Modules
```bash
mvn clean install
```

### Start the Server
```bash
cd server
mvn spring-boot:run
```

The server will start on `http://localhost:8080` with GraphQL endpoint at `/graphql`.
