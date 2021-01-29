Passive Listening Prototype
===========================

This is the main repo for the passive listening prototype.

## Structure

- buildSrc - build settings shared across the project
- classify_client - a small CLI client for testing the classification service
- common - code shared between subproject
- gradle - [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- hub - code that runs on the "primary" device (e.g., the Raspbery Pi).
    It's currently responsible for recording, storing data, invoking sub-services, and serving the dashboard.
- logging - a shared logging module
- proto - [Protocol Buffers](https://developers.google.com/protocol-buffers/) used by the project and the build settings that compile them
- transcribe - transcription service



## Building

To build all the subprojects, you can run `./gradlew build` from this directory.

To rebuild, you can run the same command, or rebuild specific subprojects, e.g., `./gradlew hub:build`

To save some time, you can also execute only specific build tasks, e.g., `./gradlew installDist`.
This should be sufficient if you're running the code using the helper script.


## Running

To run the current version, you'll need to launch an instance of (1) the hub and (2) the transcription service, from their respective directories.

For any of the services, there are 3 ways of running them:

1. `./gradlew run` builds and runs the code in one command. You'll need to specify the CLI args like `./gradlew run --args "--some-flag"`
2. `./gradlew installDist` then (from the project subdirectory) `./build/install/<project>/bin/<project> --some-flag`
3. `./gradlew shadowJar` then (from the project subdirectory) `java -jar ./build/libs/<project>-all.jar --some-flag`. This JAR can also be copied to other systems.

