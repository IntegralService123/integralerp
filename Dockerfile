# Estágio 1: Build (Compilação)
# Usamos o Maven com JDK 21+ para compilar (JDK 25 ainda é muito recente, 
# imagens oficiais estáveis costumam usar a 21 ou 23, que funcionam perfeitamente para compilar seu código)
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
COPY . .
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução)
# Aqui usamos o JRE para rodar a aplicação. 
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar

# Define a porta que o Spring Boot vai usar (Render usa a 8080 por padrão)
EXPOSE 8080

# Comando para rodar a aplicação passando o Profile de produção
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]