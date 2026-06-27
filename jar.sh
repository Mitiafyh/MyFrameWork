#!/bin/bash
set -e

APP_NAME="MyFrameWork"
PATH_FRAMEWORK="/home/fiorenantsoa/Documents/GitHub/MyFrameWork"
PATH_APP_TEST="/home/fiorenantsoa/Documents/S4/WebDynamique/AppTest"
PATH_TOMCAT="/home/fiorenantsoa/Documents/tomcat/tomcat"

cd $PATH_FRAMEWORK
mkdir -p bin
echo "Compilation du Servlet..."
javac -cp "$PATH_FRAMEWORK/lib/servlet-api.jar" -d bin src/main/java/annotation/*.java src/main/java/Presentation/*.java src/main/java/Utils/*.java

jar cvf lib/$APP_NAME.jar -C bin .

echo "creation dossier "
TARGET_LIB="$PATH_APP_TEST/lib"
mkdir -p "$TARGET_LIB"

echo Deploiement vers AppTest...
cp "lib/$APP_NAME.jar" "$TARGET_LIB/"
cp "lib/servlet-api.jar" "$TARGET_LIB/"


echo ==========================================
echo   DEPLOYE AVEC SUCCES !
echo ==========================================
