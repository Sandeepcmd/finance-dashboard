@REM --------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM --------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE__=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0telerik'; $env:MVNW_REPOURL; if($env:MVNW_REPOURL){$mvnUrl = $env:MVNW_REPOURL} else {$mvnUrl = 'https://repo.maven.apache.org/maven2'}; $wrapperJar = Join-Path '%~dp0' '.mvn\wrapper\maven-wrapper.jar'; if(!(Test-Path $wrapperJar)) {$wrapperUrl = ''; foreach($line in (Get-Content (Join-Path '%~dp0' '.mvn\wrapper\maven-wrapper.properties'))) {if($line -match '^wrapperUrl=(.+)$') {$wrapperUrl = $Matches[1]; break}}; if(!$wrapperUrl) {$wrapperUrl = $mvnUrl + '/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar'}; Write-Host \"Downloading: $wrapperUrl\"; (New-Object Net.WebClient).DownloadFile($wrapperUrl, $wrapperJar)}; $distributionUrl = ''; foreach($line in (Get-Content (Join-Path '%~dp0' '.mvn\wrapper\maven-wrapper.properties'))) {if($line -match '^distributionUrl=(.+)$') {$distributionUrl = $Matches[1]; break}}; Write-Output \"MVNW_DIST=$distributionUrl\"}"`) DO @IF "%%A"=="MVNW_DIST" SET MVNW_DIST=%%B
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE__%

@SET MVNW_JAVA_COMMAND=java
@IF NOT "%JAVA_HOME%"=="" SET MVNW_JAVA_COMMAND=%JAVA_HOME%\bin\java

@SET WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"

%MVNW_JAVA_COMMAND% ^
  %MVNW_JAVA_OPTIONS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0" ^
  org.apache.maven.wrapper.MavenWrapperMain ^
  %*
IF %ERRORLEVEL% NEQ 0 GOTO error
GOTO end

:error
SET ERROR_CODE=%ERRORLEVEL%

:end
@ENDLOCAL & SET ERROR_CODE=%ERROR_CODE%

EXIT /B %ERROR_CODE%
