#  VUIIS DataType genProcData Plugin #

This plugin provides the XNAT datatype and accompanying webpages for a custom
imageAssessorData type named genProcData. This datatype is used by DAX to
store processed imaging data.

# Building #

To build the plugin:

1. Clone this repository and cd to the newly cloned folder.
1. Build the plugin: `./gradlew jar`. This will build the plugin in the file **build/libs/vuiis-datatype-genProcData-plugin-X.Y.Z.jar** where X.Y.Z is the version.

You can update the gradle wrapper with:
./gradlew wrapper --gradle-version X.Y

If there are issues building, try deleting gradle cached files in ~/.gradle.
XNAT requires Java 8 JDK instead of the system default of 10+. To set in mac os:
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Deploying #

1. Copy the plugin jar to the plugins folder: `scp build/libs/vuiis-datatype-genProcData-plugin-X.Y.Z.jar xnat/plugins`
1. Restart tomcat
