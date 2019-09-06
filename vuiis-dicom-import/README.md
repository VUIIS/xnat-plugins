# VUIIS DICOM Import Plugin #

This is a plugin provides custom behavior for importing DICOM into XNAT. 
If the DICOM has a value in the PatientComments field, the default XNAT behavior will be run. 
Otherwise, it will parse the PatientID field to extract the Project and Subject values where the value should be 
PROJECT_Subject. Session will use the same value as Subject.


# Building #

To build the plugin:

1. If you haven't already, clone this repository and cd to the newly cloned folder.
1. Build the plugin: `./gradlew jar`. This will build the plugin in the file **build/libs/
vuiis-dicom-import-plugin-X.Y.Z.jar** where X.Y.Z is the version.
1. Copy the plugin jar to your plugins folder: `cp build/libs/vuiis-dicom-import-plugin-X.Y.Z.jar xnat/home/plugins`

# Deploying #

Deploying your XNAT plugin just requires copying it to the **plugins** folder for your XNAT installation. 
Once you've copied the plugin jar into your XNAT's **plugins** folder, you need to restart the Tomcat server. 
Your new plugin will be available as soon as the restart and initialization process is completed.
