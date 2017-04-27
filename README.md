# Thinkter: A Kotlin Full-stack Application Example

Thinkter is an example of a full-stack Web application built with Kotlin. The backend runs on the JVM, and the browser
front-end uses React. The example implements a very simple microblogging application.

## Backend

The backend of Thinkter is built using [Ktor](https://github.com/kotlin/ktor), a Web framework built by the Kotlin team.
For data persistence, it uses [H2](http://www.h2database.com), allowing you to run the example without the need to configure
an external SQL server. The HTTP server implementation is provided by [Jetty](http://www.eclipse.org/jetty/).

To run the backend, use `./gradlew backend:run`, or open Thinkter as a project in IntelliJ IDEA and execute the shared 
run configuration `Backend :: Jetty`. This will start serving the REST API of the backend on port 9090.

## Frontend

The frontend of Thinkter is built using [React](https://facebook.github.io/react/). To adapt the React APIs to Kotlin,
it incorporates a set of [wrappers](https://github.com/orangy/thinkter/tree/master/frontend/src/org/jetbrains/react), which
you can also use in your projects and adapt to your needs.

The project is built using webpack and the [Kotlin frontend plugin](https://github.com/kotlin/kotlin-frontend-plugin). 

To run the frontend, use `./gradlew frontend:run`. This will start a webpack server on port 8080. Navigate to http://localhost:8080 
to start using the application.
