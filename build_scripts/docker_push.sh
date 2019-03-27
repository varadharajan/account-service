#!/bin/bash

set -e

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push varadharajan/account-service:$CI_COMMIT_SHA
docker push varadharajan/account-service:latest
