# ===================================================================
# ESTÁGIO 1: "Build"
# Usa o JDK 21 (como no seu pom.xml) para compilar o projeto
# ===================================================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Instala o curl (útil para testes, se precisar)
RUN apk add --no-cache curl

# Copia os arquivos do Maven Wrapper e o pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixa as dependências (cache)
RUN ./mvnw dependency:go-offline

# Copia o código-fonte e compila o projeto
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ===================================================================
# ESTÁGIO 2: "Final"
# Usa uma imagem JRE leve, mas agora com curl instalado
# ===================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# ✅ Instala o curl para que o healthcheck funcione
RUN apk add --no-cache curl

# Define o perfil ativo para o Spring Boot
ENV SPRING_PROFILES_ACTIVE=docker

# Expõe a porta da aplicação
EXPOSE 8080

# Copia o .jar da etapa de build
COPY --from=build /app/target/delivery-api-0.0.1-SNAPSHOT.jar app.jar

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
