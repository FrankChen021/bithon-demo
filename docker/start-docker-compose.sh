# NOTE: the `pull` option makes sure we're using the latest image(https://hub.docker.com/r/bithon/server/tags) of Bithon server
cd docker && docker compose pull bithon-server
cd ..
docker compose -f docker/docker-compose.yml up