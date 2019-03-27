#!/bin/bash

set -e

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push varadharajan/account-service:$TRAVIS_BUILD_NUMBER
docker push varadharajan/account-service:latest
