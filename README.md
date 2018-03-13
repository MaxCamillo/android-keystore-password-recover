# android-keystore-password-recover
Project Page:
http://maxcamillo.github.io/android-keystore-password-recover/

## Build instructions

Make sure you have the Java SDK installed. Google it for install files and
usage instructions.

Then to build, execute this:

`javac -d ./build *.java`

Then to package that into a JAR execute this:

`cd build`

`jar cvfm akpr.jar ../manifest.mf AndroidKeystoreBrute/*.class`

To run:

`java -jar akpr.jar`
