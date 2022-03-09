#
# Run this script from the root of the bithon-demo project
#
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/gateway -f docker/Dockerfile-gateway .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/user-service -f docker/Dockerfile-service .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/user-client -f docker/Dockerfile-client .

docker images | grep bithon/demo