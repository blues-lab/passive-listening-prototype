Passive Listening Prototype
===========================

This is the main repo for the passive listening prototype.
The applications designed for this prototype can be found in [a separate repo](https://github.com/blues-lab/passive-listening-classifiers).

## Structure

- buildSrc - build settings shared across the project
- classify_client - a small CLI client for testing the classification service
- common - code shared between subproject
- gradle - [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- hub - code that runs on the "primary" device (e.g., the Raspbery Pi).
    It's currently responsible for recording, storing data, invoking sub-services, and serving the dashboard.
- logging - a shared logging module
- proto - [Protocol Buffers](https://developers.google.com/protocol-buffers/) used by the project and the build settings that compile them
- transcribe - transcription service using wav2letter
- transcribe_aws - transcription service using AWS cloud transcription
- transcribe_chrome - transcription service using Chrome SODA
- vad_service - standalone voice activity detection service (written in Python, talks to the rest of the project over gRPC)



## Building

To build all the subprojects, you can run `./gradlew build` from this directory.

To rebuild, you can run the same command, or rebuild specific subprojects, e.g., `./gradlew hub:build`

To save some time, you can also execute only specific build tasks, e.g., `./gradlew installDist`.
This should be sufficient if you're running the code using the helper script.


## Running

To run the current version, you'll need to launch the following:

1. an instance of the hub, from the `hub` directory
2. an instance of the transcription service, from the `transcribe` directory
3. an instance of the Voice Activity Detection service, from the `vad_service` repo. Note that it's written in Python and is launched differently from the other services.
4. a classifier (or several). These are in a separate repo.

For any of the services, there are 3 ways of running them:

1. `./gradlew run` builds and runs the code in one command. You'll need to specify the CLI args like `./gradlew run --args "--some-flag"`
2. `./gradlew installDist` then (from the project subdirectory) `./build/install/<project>/bin/<project> --some-flag`
3. `./gradlew shadowJar` then (from the project subdirectory) `java -jar ./build/libs/<project>-all.jar --some-flag`. This JAR can also be copied to other systems.
