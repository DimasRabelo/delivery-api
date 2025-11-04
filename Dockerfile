# ===================================================================
# ESTÁGIO 1: "Build"
# Usa o JDK 21 (como no seu pom.xml) para compilar o projeto
# ===================================================================
FROM eclipse-temurin:21-jdk-alpine AS build

# Define o diretório de trabalho
WORKDIR /app

RUN apk add --no-cache curl

# Copia os arquivos do Maven Wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixa as dependências (camada de cache)
# Isso só será executado novamente se o pom.xml mudar
RUN ./mvnw dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Compila o projeto, gera o .jar e pula os testes
RUN ./mvnw clean package -DskipTests

# ===================================================================
# ESTÁGIO 2: "Final"
# Usa uma imagem JRE (Runtime) leve, pois não precisamos mais do JDK
# ===================================================================
# Usando eclipse-temurin que é uma JRE Alpine comum e leve
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Define a variável de ambiente para ativar o perfil "docker"
# Isso fará o Spring Boot ler o 'application-docker.properties'
ENV SPRING_PROFILES_ACTIVE=docker

# Expõe a porta que sua aplicação usa (definida no application.properties)
EXPOSE 8080

# Copia o .jar gerado no estágio "build" para a imagem final
# Note o nome do jar, pego do seu pom.xml
COPY --from=build /app/target/delivery-api-0.0.1-SNAPSHOT.jar app.jar

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]