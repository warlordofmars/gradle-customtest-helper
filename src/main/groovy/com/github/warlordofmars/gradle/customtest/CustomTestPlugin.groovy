package com.github.warlordofmars.gradle.customtest


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

import groovy.xml.MarkupBuilder
import groovy.util.XmlSlurper
import groovy.xml.XmlUtil

import java.io.StringWriter

import com.github.warlordofmars.gradle.customtest.model.Test

class CustomTestPlugin implements Plugin<Project> {

    void apply(Project project) {

        if(!project.tasks.findByName('clean')) {
            project.task('clean', type:Delete) {
                delete project.buildDir
            }
        }

        project.task('registerTests') {
            description 'Register all configure tests'
            group 'Test'
            doFirst {

                project.rootProject.ext.getTestCounts = {
                    def total = 0
                    def skipped = 0
                    def failed = 0
                    def success = 0
                    project.subprojects.each { p ->
                        if(p.hasProperty('tests')) {
                            p.tests.each { name, test ->
                                total++
                                def testObj = project.ext."${name}"
                                if(!testObj.skipped) {
                                    if(!testObj.success) {
                                        failed++
                                    } else {
                                        success++
                                    }
                                } else {
                                    skipped++
                                }
                            }
                        }
                    }
                    return [
                        total: total,
                        skipped: skipped,
                        failed: failed,
                        success: success
                    ]
                }
                
                def sw = new StringWriter()
                def xml = new MarkupBuilder(sw)
                xml.testsuites {
                    project.subprojects.each { p ->
                        if(p.hasProperty('tests')) {
                            testsuite(tests: p.tests.size()) {
                                p.tests.each { name, test ->
                                    project.ext."${name}" = new Test(test[0], test[1])
                                    testcase(classname: test[0], name: test[1]) {
                                        skipped()
                                    }
                                }
                            }
                        }
                    }
                }

                def reportFile = project.file("${project.buildDir}/report.xml")
                if(!reportFile.exists()) {
                    project.file("${project.buildDir}").mkdirs()
                    reportFile.write(sw.toString())
                }

            }
        }

        project.getGradle().buildFinished {
            project.file("${project.buildDir}").mkdirs()
            def reportFile = project.file("${project.buildDir}/report.xml")
            if(reportFile.exists()) {
                def xml = new XmlSlurper().parseText(reportFile.text)

                xml.testsuite.each { testsuite ->
                    testsuite.testcase.each { testcase ->
                        project.subprojects.each { p ->
                            if(p.hasProperty('tests')) {
                                p.tests.each { name, test ->
                                    if(project.ext.has(name)) {
                                        def testObj = project.ext."${name}"
                                        if(!testObj.skipped) {
                                            if("${testObj.classname}" == "${testcase.@classname}" && "${testObj.name}" == "${testcase.@name}") {
                                                println "Found test result: ${testcase.@name}"
                                                testcase.skipped.replaceNode { }
                                                if(testObj.output) {
                                                    testcase.appendNode {
                                                        'system-out'(testObj.output)
                                                    }
                                                }
                                                if(!testObj.success) {
                                                    testcase.appendNode {
                                                        failure(type: testObj.failureType, details: testObj.output)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                }
                            }
                        }
                    }
                }
                reportFile.withWriter { outWriter ->
                    XmlUtil.serialize(xml, outWriter)
                }

                println "\nSaved generated test report file to: ${reportFile}"
            
            }

            
        }

    }

}