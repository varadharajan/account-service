# Account-service [![Build Status](https://travis-ci.com/varadharajan/account-service.svg?token=N74RQfb9r5KiVz1SyvGt&branch=master)](https://travis-ci.org/varadharajan/account-service)

A RESTful service to demonstrate money transfer between accounts

## Dependencies

1. OpenJDK 8+
2. sbt 1.2.x+

## Try out the service without cloning the repo!

```bash
$ cat << EOF > application.conf
account_service {
  server {
    host = "0.0.0.0"
    port = 8080
  }
}
EOF

$ docker run -v $(pwd)/application.conf:/apps/account-service/application.conf -p 8080:8080 -ti varadharajan/account-service:latest
```

You should be able to access the API at http://localhost:8080 (For example: http://localhost:8080/v1/accounts). Pressing RETURN key would stop the container.

Here are some API calls:

```bash
# Create two accounts
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"varadharajan"}' http://localhost:8080/v1/accounts
$ curl -H "Content-Type: application/json" -X POST -d '{"name":"srinath"}' http://localhost:8080/v1/accounts
# From the above responses, we can get the account ID of those users

# Get all users in the system
$ curl http://localhost:8080/v1/accounts

# Get details of an account
$ curl http://localhost:8080/v1/accounts/<account-UUID>

# Fund transfer to accounts
$ curl -X PUT "http://localhost:8080/v1/accounts/<account-UUID>/deposit?amount=100"
$ curl -X PUT "http://localhost:8080/v1/accounts/<account-UUID>/withdraw?amount=100"

# Transfer money from one account to another
$ curl -H "Content-Type: application/json" -X POST -d '{"from":"<account-UUID>","to": "<account-UUID>","amount": 50}' http://localhost:8080/v1/accounts/transfer
```


## Local Development

1. The project supports sbt + JDK style of development, provided compatible version of dependencies

```bash
$ sbt test # To run the tests
$ sbt assembly # To build the fat jar. Artifact will be @ $PROJECT_DIR/target/
```

2. Docker can be used to build an executeable artifact

```bash
$ docker build -t account-service:dev .

$ cat << EOF > application.conf
account_service {
  server {
    host = "0.0.0.0"
    port = 8080
  }
}
EOF

$ docker run -v $(pwd)/application.conf:/apps/account-service/application.conf -p 8080:8080 -ti account-service:dev
```

## CI / CD

1. The project has been configured to use Travis CI as the CI service. On every commit / PR merge to master branch, we build the docker container, run tests inside it, and then push the newly built container to Dockerhub. *Please note that, this DockerHub repository will be deleted after receiving coding assignment feedback*.

2. Checkout the builds @ https://travis-ci.com/varadharajan/account-service/

3. Checkout the Dockerhub repo @ https://hub.docker.com/r/varadharajan/account-service/tags

## Considerations

1. We make use of [Software Transactional Memory](https://en.wikipedia.org/wiki/Software_transactional_memory) using [Scala STM](https://nbronson.github.io/scala-stm/index.html) as the underlying concurrency control mechanism for in-memory datastructures. 

2. The service is implemented in a completely non-blocking, asynchronous fashion using Futures and Akka HTTP (Server + Client). 

3. The codebase is roughly split into the below segments:
  * Model.scala : Contains the core domain models and API Request + Response SerDe utils.
  * Repository.scala : Persistence layer for core models. Its straightforward in this case because of in-memory datastructures and the use of STM, however we need to revist them a bit if we want to add persistence.
  * Service.scala : Contains the API mashup layer taking to rest of classes. Also contains the Routers defining the routes for the interfaces.
