@echo off
REM This script runs the GreenCompostWaste application with JavaFX

REM Check if JavaFX SDK path is set
if "%JAVAFX_HOME%"=="" (
  echo Please set JAVAFX_HOME environment variable to your JavaFX SDK path
  echo Example: set JAVAFX_HOME=C:\path\to\javafx-sdk
  exit /b 1
)

REM Compile the project
echo Compiling GreenCompostWaste...
javac --module-path %JAVAFX_HOME%\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -d bin src\module-info.java src\Main.java src\com\greencompost\*.java src\com\greencompost\controller\*.java src\com\greencompost\service\*.java src\controllers\*.java

REM Run the application
echo Running GreenCompostWaste...
java --module-path %JAVAFX_HOME%\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp bin Main