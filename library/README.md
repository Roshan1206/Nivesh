# Library
Common library for nivesh. Files under configuration or service directly will be auto configured.

This module includes the following files which has to shared across 2 or more services.
* Configuration files
* Constants
* Base entity and status enums
* Exceptions
* Service files

### Build and publish the jar
```shell
    ./gradlew clean build publishToMavenLocal
```