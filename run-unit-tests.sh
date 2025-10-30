#!/bin/bash
# Roda APENAS os testes unitários (rápidos).
# Exclui tudo que termina com 'IntegrationTest'.

set -e 

echo "🚀 Executando APENAS Testes Unitários (Rápido)..."

# O comando -Dtest=!*IntegrationTest exclui os testes de integração
mvn clean test -Dtest=!*IntegrationTest

echo "--------------------------------------------------------"
echo "✅ Testes unitários concluídos com sucesso!"
echo "--------------------------------------------------------"