# VUIIS DICOM Import Plugin #

This plugin provides custom behavior for importing DICOM into the XNAT at VUIIS.
If the DICOM has a value in the PatientComments field, the default XNAT behavior will be run. 
Otherwise, it will parse the PatientID field to extract the Project and Subject values where the syntax should be 
PROJECT_Subject. Session will use the same value as Subject.


# Building #

To build the plugin:

1. Clone this repository and cd to the newly cloned folder.
1. Build the plugin: `./gradlew jar`. This will build the plugin in the file **build/libs/vuiis-dicom-import-plugin-X.Y.Z.jar** where X.Y.Z is the version.

# Deploying #

1. Copy the plugin jar to the plugins folder: `scp build/libs/vuiis-dicom-import-plugin-X.Y.Z.jar xnat:~/xnat/home/plugins`
1. Restart tomcat
