#!/bin/bash
# Roda APENAS os testes de integração (*IntegrationTest).
# Ativa o perfil de teste para o banco H2.

set -e

echo "🚀 Executando APENAS Testes de Integração (*IT)..."

# O comando -Dtest=*IntegrationTest roda SÓ os testes de integração
mvn clean test -Dspring.profiles.active=test -Dtest=*IntegrationTest

echo "--------------------------------------------------------"
echo "✅ Testes de integração concluídos com sucesso!"
echo "--------------------------------------------------------"