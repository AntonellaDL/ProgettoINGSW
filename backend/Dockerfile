# Usa un'immagine con Maven e JDK 21 per compilare il progetto
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamo prima solo il pom.xml per sfruttare la cache dei layer Docker:
# le dipendenze vengono riscaricate solo se il pom.xml cambia
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamo il resto del codice sorgente e compiliamo il jar,
# saltando i test (già eseguiti in fase di sviluppo/CI)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Immagine leggera con solo la JRE, senza Maven e senza i sorgenti,
# per ridurre le dimensioni dell'immagine finale
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamo dallo stage di build solo il jar generato
COPY --from=build /app/target/*.jar app.jar

# Cartella usata da FileStorageService/WebConfig per gli allegati delle issue.
# Creata esplicitamente per poter essere montata come volume esterno.
RUN mkdir -p /app/uploads

# Porta esposta di default da Spring Boot
EXPOSE 8080

# Avvio dell'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]