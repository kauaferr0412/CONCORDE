# --- build: compila o jar com Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixa as dependencias antes de copiar o codigo -> cache do Docker so' invalida essa camada
# quando o pom.xml muda, nao a cada alteracao de codigo.
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# --- runtime: so' o JRE + o jar, imagem final bem menor ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
