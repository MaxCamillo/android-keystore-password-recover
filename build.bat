@echo off
SET PATH=%PATH%;H:\Jarno\Downloads\openjdk-10.0.1_windows-x64_bin\jdk-10.0.1\bin
javac -d ./build *.java
cd build
jar cvfm akpr.jar ../manifest.mf *.class
cd ..