# ---------------------------
# Stage 1 : Build Native
# ---------------------------
FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:22.3-java17 AS build

# Copier les scripts maven wrapper et config
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn

# Copier le fichier pom.xml
COPY --chown=quarkus:quarkus pom.xml /code/

USER quarkus
WORKDIR /code

# Copier le code source
COPY --chown=quarkus:quarkus src /code/src

# Build natif
RUN ./mvnw package -Pnative -Dquarkus.native.container-build=true

# ---------------------------
# Stage 2 : Runtime ultra léger
# ---------------------------
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app/

# Copier le binaire natif et donner les droits à l'utilisateur non-root
COPY --from=build --chown=1001:root --chmod="g+rwX" /code/target/*-runner /app/application

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]