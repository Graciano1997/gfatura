#!/bin/bash
# Diretórios
SRC_DIR=src
BIN_DIR=bin
LIB_DIR=lib
JAR_NAME=gfatura.jar
MANIFEST=manifest.txt

# Limpa o diretório bin
rm -rf "$BIN_DIR"
mkdir "$BIN_DIR"

# Compila os arquivos Java
echo "Compilando arquivos Java..."
javac -cp "$LIB_DIR/*" -d "$BIN_DIR" "$SRC_DIR"/*.java

# Verifica se a compilação foi bem-sucedida
if [ $? -ne 0 ]; then
    echo "❌ Erro ao compilar. Abortando."
    exit 1
fi

# Copia os .jar de dependências para bin
echo "Copiando dependências..."
cp "$LIB_DIR"/*.jar "$BIN_DIR"

# Cria o manifest.txt com TODAS as dependências
echo "Criando manifest..."
cat > "$MANIFEST" <<EOL
Main-Class: Main
Class-Path: jackson-core-2.17.0.jar jackson-databind-2.17.0.jar jackson-annotations-2.17.0.jar openhtmltopdf-core-1.0.10.jar openhtmltopdf-pdfbox-1.0.10.jar pdfbox-2.0.30.jar fontbox-2.0.30.jar commons-logging-1.2.jar jsoup-1.15.3.jar

EOL

# Cria o .jar final
echo "Gerando $JAR_NAME..."
cd "$BIN_DIR"
jar cfm "$JAR_NAME" "../$MANIFEST" *.class

# Volta ao diretório raiz
cd ..

# Concluído
echo "✅ Compilação e empacotamento concluídos. Arquivo criado: bin/$JAR_NAME"
echo "👉 Para executar: cd bin && java -jar $JAR_NAME"