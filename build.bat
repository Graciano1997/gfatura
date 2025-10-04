@echo off
REM Diretórios
set SRC_DIR=src
set BIN_DIR=bin
set LIB_DIR=lib
set JAR_NAME=gfatura.jar
set MANIFEST=manifest.txt

REM Limpa o diretório bin
if exist "%BIN_DIR%" rmdir /s /q "%BIN_DIR%"
mkdir "%BIN_DIR%"

REM Compila os arquivos Java
echo Compilando arquivos Java...
javac -cp "%LIB_DIR%\*" -d "%BIN_DIR%" "%SRC_DIR%\*.java"

if errorlevel 1 (
    echo ❌ Erro ao compilar. Abortando.
    exit /b 1
)

REM Copia dependências (opcional)
copy "%LIB_DIR%\*.jar" "%BIN_DIR%" >nul

REM Cria o manifest.txt
echo Criando manifest...
(
    echo Main-Class: Main
    echo Class-Path: jackson-core-2.17.0.jar jackson-databind-2.17.0.jar jackson-annotations-2.17.0.jar
) > "%MANIFEST%"

REM Cria o .jar final
echo Gerando %JAR_NAME%...
cd "%BIN_DIR%"
jar cfm "%JAR_NAME%" "..\%MANIFEST%" *.class

cd ..

REM Concluído
echo ✅ Compilação e empacotamento concluídos. Arquivo criado: bin\%JAR_NAME%
echo 👉 Para executar: cd bin && java -jar %JAR_NAME%
pause
