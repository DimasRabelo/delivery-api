#!/bin/bash

# A MÁGICA DA AUTOMAÇÃO:
# 'set -e' faz com que o script pare imediatamente se QUALQUER comando falhar.
# Se o 'mvn clean test' falhar, ele não tentará gerar o relatório.
set -e

echo "Iniciando o processo completo de build e testes..."

# 1. Limpa o projeto (clean)
# 2. Executa todos os testes (test)
# 3. Ativa o perfil 'test' (você mencionou que o application.properties está pronto)
# 4. Gera o relatório de cobertura (jacoco:report)
# 5. Verifica se a cobertura mínima foi atingida (jacoco:check)

mvn clean test -Dspring.profiles.active=test jacoco:report jacoco:check

echo "--------------------------------------------------------"
echo "✅ Script concluído com sucesso!"
echo "Build, testes e análise de cobertura passaram."
echo "--------------------------------------------------------"

# Comando para abrir o relatório no Linux (opcional)
echo "Para visualizar o relatório de cobertura, execute:"
echo "xdg-open target/site/jacoco/index.html"