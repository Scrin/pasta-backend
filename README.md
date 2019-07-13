# Pasta backend

Backend for the [pasta-frontend](https://github.com/Scrin/pasta-frontend).

### Requirements

- JDK 11 (During development/building, JRE 11 is enough for running the built package)
- Maven (During development/building only)

### Development

- Set up a redis instance somewhere
- Add `redis` to your /etc/hosts to point to the host running Redis (127.0.0.1 if you're running it locally) **OR** pass `PASTA_REDIS_HOST=xxx.xxx.xxx.xxx` environment variable (can be localhost if Redis is running locally)
- Final package can be built with `mvn package` and the final package can be run with something like `java -jar pasta-backend-*.jar`

### Docker

The entire "pasta setup" is designed to be run in Docker containers, `docker-compose.yml` contains a sample Compose file that will run the frontend, backend and Redis database.
