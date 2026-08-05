@echo off
if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
  java -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
  exit /b %ERRORLEVEL%
)
echo gradle-wrapper.jar is not included in this generated workspace. Open the project in Android Studio and let it configure the Gradle wrapper, or install Gradle 8.7 and run it from the project root.
exit /b 1
