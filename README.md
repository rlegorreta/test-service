# <img height="25" src="./images/AILLogoSmall.png" width="40"/> Spring Authorization Server

<a href="https://www.legosoft.com.mx"><img height="160px" src="./images/AuthServerLogo.png" alt="AI Legorreta" align="left"/></a>
This microservice utilizes Spring Authorization Server project 1.0.0  (i.e., Spring Boot 3.1.0).
To use the latest version this microservice utilizes latest security standards contrary to the almost deprecated version
of the Oauth2 server. It utilizes Spring Security 6, Open ID Connect and Oauth 2.1

* Local no docker: `localNoDocker` platform tu run the microservice inside the IDE.
* Local docker: `local` on-premise (docker desktop).

This project is a template to build Oauth2 server in order to generate tokens for login and token validation between
Spring Gateway or any microservice that it is declared as a protected resource (e.g., iam-service microservice).

Other platforms does not use Spring Config Server but Kubernetes ConfigMap & Secret.

## Introduction

The Spring Authorization server has the exact role as an external authorization like `Google Sign In` or the
`KeyCloak` solutions.

The server looks for an existing user and a valid password using a LDAP Directory. All the permits are defined
in the graph database `Neo4j`. The administration for this database is NOT done by this microservice, 
the administration is done by `iam-service` and `iam-ux` microservices.

In order to be more efficient the access to `Neo4j` database, this microservice does NOT call the `iam-service`
but access the database directly.


### Dependencies

Auth-service microservice uses IAM Neo4j database for user authentication.

The Neo4j database could be stored in the same `docker-compose.yml` but for performance reasons and because normally the Neo4j
is administrated by other microservices it is stored as a separated image (or pod).

The authorization server microservice has several REST endpoints:

- REST service to validate a JWT token and returns the user, permits and company. This REST is utilized for microservice
  that want to read the information that it is inside de JWT token.
- REST to check the token users.
- Authorization server default endpoints

         * Authorization EndPoint           /oauth2/authorize
         * Token EndPoint                   /oauth2/token
         * Token Revocation                 /oauth2/revoke
         * Token Introspection              /oauth2/introspect
         * JWK Set EndPoint                 /oauth2/jwks
         * Authorization Server Metadata    /.well-known/oauth-authorization-server
         * OIDC Provider Configuration      /.well-known/openid-configuration

The token standard format is JWT (Java Web Token).

The JWT token is an extended token that also returns the user company and also its permits.
No need to ask iamserverrepo microservice for user permits.

### Compilation note: Neo4jDB to get all permits

When this microservice is compiled it sends the following warning:

```
The entity XXXXXXX is using a Long value for storing internally generated Neo4j ids. The Neo4j internal Long Ids are deprecated, please consider using an external ID generator.
```

As for documentation in SDN Neo4j it still utilizes Long es generated value and in :

```
https://community.neo4j.com/t5/drivers-stacks/list-of-relationshipproperties/m-p/61214
```

Is says Please ignore this warning for now because we are still not sure if we will just stick with the old id format
and dismiss this warning in the GA version.

This maybe change for Neo4j 5 and SDN 7

### Test installation

In order to check that the Authorization server microservice we need to follow the URL:

http://localhost:9000/oauth/token       to get the token with a valida username password and application.

note: The Neo4j database needs to be started in order to get a token.

Other REST service (documented in swagger) are:

http://localhost:9000/usuario             to get the JWT token values (e.g., company and permits)

http://localhost:9000/validar?token       validates the token and return the JWT values (also validates that is its not expire)

http://localhost:9000/fechaOperacion            gets the systema operation date

http://localhost:9000/addDate             increments the system date to the next date (no holidays are validated)

## How to get a valid Token

### Calling from UI

In order to get a valid token called form a UI (i.e, scope = webclient o mobileclient) we need to call the following REST POST service:


http://localhost:9000/oauth/token

With the following parameters in the http header:
- Authorization:
    - Basic Auth
    - Username: valid AppName are: iam, tecval
    - Password: encrypted in the .ym files.
- Body:
    - key: grant_type   value: password
    - key: scope        value: webclient
    - key: username     value: <valid user existing in LDAP and IAM>
    - key: password     value: <valid password in LDAP>

For example of this call you can use the Postman directly.

### Calling from a microservice

There are many cases where a REST called is needed from a mrico.service to other micro.service without ant intervetion from the UI.
Therefore, 'no' token exists.

For example: A microservice is a listener for some EVENT (in the event-logger) and after processing the event, we need to send another event.
Like a system error.

In this example the listener does not have any token and it needs to get one. In order to get a valid token this micro.service needs to call a REST POST:

http://localhost:8901/oauth/token

With the following parameters in the http header:
- Authorization:
    - Basic Auth
    - Username: valid AppName are: tecvalBack, notificationsClient
    - Password: encrypted in the .ym files.

- Body:
  key: grant_type   value: client_credentials

For example for this type of call see: iam-server-repo.

### Reference Documentation
This microservice uses the recent Spring Authorization Server (0.2.0) :

* [Spring Authorization Server project](https://github.com/spring-projects/spring-authorization-server/)
* [Samples](https://github.com/spring-projects/spring-authorization-server/blob/main/samples)
* [Getting Started with Spring Authorization Server video](https://www.youtube.com/watch?v=3NliXoTcPSo/)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.0.1/reference/htmlsingle/#production-ready)
* [Config Client Quick Start](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_client_side_usage)
* [Eureka Discovery Client](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.0.1/reference/htmlsingle/#boot-features-security)

### Authorization server database

In order to register the clients instead of using a memory schema (which is not recommended for production environment)
the microservice uses a Postgres database called authdb.

A separate Postgres image is created for authdb:

* A script called 01_init.sh is run when the image is created and creates the authdb with a new user.
* The microservice uses Flyway to create the tables. The scrip is stored in /resource/db/migration

#### Important note: Troubleshooting PostgresSQL Docker

In the docker-compose.yml file que run a script for the first time called 01-init.sh,  sometimes we received an
error like thisÑ 'docker-entrypoint-initdb.d permission denied'.

The solution of the proble is that the file 01-init.sh needs to have execution permissions, so the instruction:

```unix
chmod +x ./src/main/docker/security/db/01-init.sh
```

## Running on Docker Desktop

### Create the image manually

```
./gradlew bootBuildImage
```

### Publish the image to GitHub manually

```
./gradlew bootBuildImage \
   --imageName ghcr.io/rlegorreta/auth-service \
   --publishImage \
   -PregistryUrl=ghcr.io \
   -PregistryUsername=rlegorreta \
   -PregistryToken=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
```

### Publish the image to GitHub from the IntelliJ

To publish the image to GitHub from the IDE IntelliJ a file inside the directory `.github/workflows/commit-stage.yml`
was created.

To validate the manifest file for kubernetes run the following command:

```
kubeval --strict -d k8s
```

This file compiles de project, test it (for this project is disabled for some bug), test vulnerabilities running
skype, commits the code, sends a report of vulnerabilities, creates the image and lastly push the container image.

<img height="340" src="./images/commit-stage.png" width="550"/>

For detail information see `.github/workflows/commit-stage.yml` file.


### Run the image inside the Docker desktop

```
docker run \
    --net ailegorretaNet \
    -p 9000:9000 \
    -e SPRING_PROFILES_ACTIVE=local \
    docker.io/ailegorreta/auth-service
```

Or a better method use the `docker-compose` tool. Go to the directory `ailegorreta-deployment/docker-platform` and run the command:

```
docker-compose up
```

## Run inside Kubernetes

### Manually

If we do not use the `Tilt`tool nd want to do it manually, first we need to create the image:

Fist step:

```
./gradlew bootBuildImage
```

Second step:

Then we have to load the image inside the minikube executing the command:

```
image load ailegorreta/auth-service --profile ailegorreta 
```

To verify that the image has been loaded we can execute the command that lists all minikube images:

```
kubectl get pods --all-namespaces -o jsonpath="{..image}" | tr -s '[[:space:]]' '\n' | sort | uniq -c\n
```

Third step:

Then execute the deployment defined in the file `k8s/deployment.yml` with the command:

```
kubectl apply -f k8s/deplyment.yml
```

And after the deployment can be deleted executing:

```
kubectl apply -f k8s/deplymengt.yml
```

Fourth step:

For service discovery we need to create a service applying with the file: `k8s/service.yml` executing the command:

```
kubectl apply -f k8s/service.yml
```

And after the process we can delete the service executing:

```
kubectl deltete -f k8s/service.yml
```

Fifth step:

If we want to use the project outside kubernetes we have to forward the port as follows:

```
kubectl port-forward service/auth-service 9000:80
```

Appendix:

If we want to see the logs for this `pod` we can execute the following command:

```
kubectl logs deployment/config-service
```

### Using Tilt tool

To avoid all these boilerplate steps is much better and faster to use the `Tilt` tool as follows: first create see the 
file located in the root directory of the proyect called `TiltFile`. This file has the content:

```
# Tilt file for config-service
# Build
custom_build(
    # Name of the container image
    ref = 'config-service',
    # Command to build the container image
    command = './gradlew bootBuildImage --imageName $EXPECTED_REF',
    # Files to watch that trigger a new build
    deps = ['build.gradle', 'src']
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('config-service', port_forwards=['9000'])
```

To execute all five steps manually we just need to execute the command:

```
tilt up
```

In order to see the log of the deployment process please visit the following URL:

```
http://localhost:10350
```

Or execute outside Tilt the command: 

```
kubectl logs deployment/config-service
```

In order to undeploy everything just execute the command:

```
tilt down
```

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.6/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.6/gradle-plugin/reference/html/#build-image)
* [Config Server](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_spring_cloud_config_server)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.0.1/reference/htmlsingle/#production-ready)

#### Guides
The following guides illustrate how to use some features concretely:

* [Centralized Configuration](https://spring.io/guides/gs/centralized-configuration/).
* Chapter 4 of 'Cloud Native Spring in Action'.
* Chapter 6 of 'Cloud Native Spring in Action'.
* Chapter 7 of 'Cloud Native Spring in Action'.
* Book 'Spring Microservices in Action'.

#### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

#### Contact AI Legorreta

Feel free to reach out to AI Legorreta on [web page](https://legosoft.com.mx).

Version: 2.0.0
©LegoSoft Soluciones, S.C., 2023