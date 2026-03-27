Developer Instructions
======================

This program is written in Kotlin (currently using Kotlin `2.1.0`) and uses Gradle as build system. Thus, to run the
program use `./gradlew run` which will download (and cache) all the dependencies, compile the program, and execute it.
The only external program that is currently used within the program is `rclone`, which is used to download the
YRA-managed GPX file from their Google Drive.

The file containing the marks that are maintained is `San_Francisco.csv`. This file is updated manually to include
new marks, or remove no longer used marks, or update the description or position of marks. However, it is also
updated by the program to incorporate updates from the YRA GPX file and from the NOAA XML file. Thus, it makes sense
to only run the program when the file `San_Francisco.csv` is under version control (e.g. Git) and unmodified. That
way, the automated updates from YRA and NOAA can then be seen after running the program by executing `git diff 
San_Francisco.csv` (or whatever command based on the version control system you are using).

To update the program dependencies, run `./gradlew versionCatalogUpdate` which will automatically update all
library dependencies to their latest released version. The only manual updates possibly required after running this
command are:

- updating the Kotlin version in `build.gradle.kts` by updating the `kotlinVersion` value
- updating the Java version in `build.gradle.kts` by updating the `jvmToolchain` value
- updating the gradle version by running `./gradlew wrapper --gradle-version=latest`
