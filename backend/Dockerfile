# --- build: compila o jar com Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixa as dependencias antes de copiar o codigo -> cache do Docker so' invalida essa camada
# quando o pom.xml muda, nao a cada alteracao de codigo.
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# --- extract: separa o jar executavel em camadas (dependencies / application / etc).
# Necessario pra evitar um bug conhecido das libs do Google Cloud (gax) com o novo loader
# de jar do Spring Boot 3.2+: elas tentam abrir o proprio .jar como arquivo pra ler a
# versao e quebram com "URI is not hierarchical" quando esse .jar esta aninhado dentro
# do app.jar (esquema de URI "nested:"). Extraido em pastas reais, cada dependencia vira
# um .jar de verdade no disco, com URI normal - o erro some.
FROM eclipse-temurin:17-jre-jammy AS extract
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# --- runtime: so' o JRE + a aplicacao ja extraida ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
