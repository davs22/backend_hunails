# ESTÁGIO 1: Build (Compilação)
# Usa uma imagem oficial do Maven com Java 17 para baixar as dependências e gerar o .jar
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos do projeto para dentro do contêiner
COPY pom.xml .
COPY src ./src

# Executa o Maven para construir o projeto, ignorando os testes para ser mais rápido
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Run (Execução)
# Usa uma imagem minúscula do Java 17 (Alpine) apenas para rodar o app, economizando memória
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o arquivo .jar gerado no Estágio 1
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]