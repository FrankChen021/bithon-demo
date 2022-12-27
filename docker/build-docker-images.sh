#
# Run this script from the root of the bithon-demo project
#
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/gateway -f docker/Dockerfile-gateway .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/user-service -f docker/Dockerfile-user-service .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/user-client -f docker/Dockerfile-user-client .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/account-client -f docker/Dockerfile-account-client .
DOCKER_BUILDKIT=1 docker build --pull -t bithon/demo/account-service -f docker/Dockerfile-account-service .

docker images | grep bithon/demo