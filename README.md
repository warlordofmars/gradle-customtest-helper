# gradle-customtest-helper

[![latest jitpack release](https://jitpack.io/v/warlordofmars/gradle-customtest-helper.svg)](https://jitpack.io/#warlordofmars/gradle-customtest-helper)

## Overview

Gradle plugin to provide junit-style XML report for custom tests defined within custom Gradle tasks

## Setup

To use this plugin, the following buildscript repositories and dependencies must be configured:

```gradle
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.warlordofmars:gradle-customtest-helper:release-0.1.11'
  }
}
```

Then to apply the plugin:

```gradle
apply plugin: 'com.github.warlordofmars.gradle.customtest'
```

To define tests:

```gradle
ext.tests = [
    someTestName: [
        '<source file / class being tested>',
        'Descriptive Name of the Test to include in final Report.'
    ]
]
```

Then in custom gradle task that is actually applying test logic, you can access an object representation of the test using the key value, `someTestName`, defined in the `tests` mapping above.

```gradle
task('someCustomTest') {
    dependsOn registerTests
    doLast {
        try {
            // something that might fail
            someTestName.success("Looks good!")
        } catch(Exception e) {
            someTestName.failure("Error!", "Exception was caught: ${e.toString()}")
        }
    }
}
```

## Versioning

Versioning on this project is applied automatically on all changes using the [axion-release-plugin](https://github.com/allegro/axion-release-plugin).  Git tags are created for all released versions, and all available released versions can be viewed in the [Releases](https://github.com/warlordofmars/gradle-customtest-helper/releases) section of this project.

## Author

* **John Carter** - [warlordofmars](https://github.com/warlordofmars)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
