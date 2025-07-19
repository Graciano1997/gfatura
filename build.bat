@echo off
set SRC_DIR=src
set BIN_DIR=bin
set LIB_DIR=lib
set JAR_NAME=gfatura.jar
set MANIFEST=manifest.txt

echo Limpando %BIN_DIR%...
rmdir /s /q %BIN_DIR%
mkdir %BIN_DIR%

echo Compilando arquivos Java...
javac -cp "%LIB_DIR%\*" -d %BIN_DIR% %SRC_DIR%\*.java
if %errorlevel% neq 0 (
    echo ❌ Erro ao compilar. Abortando.
    exit /b 1
)

echo Copiando JARs para %BIN_DIR%...
copy %LIB_DIR%\*.jar %BIN_DIR%

echo Criando manifest...
(
    echo Main-Class: Main
    echo Class-Path: jackson-core-2.17.0.jar jackson-databind-2.17.0.jar jackson-annotations-2.17.0.jar
) > %MANIFEST%

echo Criando %JAR_NAME%...
cd %BIN_DIR%
jar cfm ..\%JAR_NAME% ..\%MANIFEST% *.class

cd ..
echo ✅ Compilado: %JAR_NAME%
echo 👉 Para executar: cd bin && java -jar %JAR_NAME%
