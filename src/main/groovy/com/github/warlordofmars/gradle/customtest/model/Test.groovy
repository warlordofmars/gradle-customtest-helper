package com.github.warlordofmars.gradle.customtest.model

import org.gradle.api.GradleException

class Test {

    String name
    String classname
    Boolean skipped
    Boolean success
    String output
    String failureType
    
    Test(classname, name) {
        this.classname = classname
        this.name = name
        this.skipped = true
    }

    void success(String output) {
        this.skipped = false
        this.success = true
        this.output = output
        println output
    }

    void failure(String type, String output) {
        this.skipped = false
        this.success = false
        this.output = output
        this.failureType = type
        throw new GradleException("${this.failureType}:\n${this.output}")
    }

}