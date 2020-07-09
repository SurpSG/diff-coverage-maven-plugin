# diff-coverage-maven-plugin
![CI](https://github.com/SurpSG/diff-coverage-maven-plugin/workflows/CI/badge.svg)
[![codecov](https://codecov.io/gh/SurpSG/diff-coverage-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/SurpSG/diff-coverage-maven-plugin)

`Diff coverage maven plugin` builds [JaCoCo](https://github.com/jacoco/jacoco) compatible code coverage report of new and modified code based on a provided [diff](https://en.wikipedia.org/wiki/Diff#Unified_format). 

The plugin is able to check and fail a build if code coverage rules are not met.

## How it works 
`Diff coverage` works in tandem with JaCoCo and requires JaCoCo to be applied to a project as well.
The plugin does the next steps:
* analyzes coverage data('.exec' files generated by JaCoCo)
* cuts off all code coverage data that wasn't modified
* checks violation rules if any are set up
* builds diff coverage HTML report. See [HTML report example](#Report-example)

## Install plugin
```xml
<build>
    <plugins>
        <plugin> <!-- Make sure JaCoCo plugin is applied -->
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.5</version>
        </plugin>
        <plugin>
            <groupId>com.github.surpsg</groupId>
            <artifactId>diff-coverage-maven-plugin</artifactId>
            <version>0.0.1</version>
            <configuration>
                
                <!-- Required. diff content source. only one of file or URL is allowed -->
                <diffSource>
                    <!-- path to diff file -->
                    <file>path/to/diffFile</file>
                
                    <!-- URL to get diff content by. Used GET method -->
                    <url>http://url.com</url>
                </diffSource>
                
                <violations> <!-- Optional -->
                    
                    <!-- Default 'false'. Fail build if violation rules weren't met  -->
                    <failOnViolation>true</failOnViolation>
                    
                    <!-- Default '0.0'. If value is '0.0' then the rule is disabled -->
                    <minLines>0.0</minLines>
                    <minBranches>0.7</minBranches>
                    <minInstructions>1.0</minInstructions>

                </violations>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>diffCoverage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Maven goal description 
`Diff Coverage Maven plugin` has a single goal `diffCoverage` that is bind to `verify` phase 
```shell script
# run tests and build diff coverage report
mvn clean verify

# Invoke the goal explicitly.
# It depends on `test` phase, make sure it's called when coverage data(*.exec files) already generated.  
mvn clean test diff-coverage:diffCoverage
```

## Report example
Maven output on failed violation rules:
```
[INFO] File src/main/kotlin/org/example/Hello.kt has 5 modified lines
[INFO] New violation: Rule violated for bundle untitled: instructions covered ratio is 0.4, but expected minimum is 0.7
[INFO] New violation: Rule violated for bundle untitled: lines covered ratio is 0.6, but expected minimum is 0.7
[WARNING] Fail on violations: true. Found violations: 2.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.919 s
[INFO] Finished at: 2020-07-06T00:56:33+03:00
[INFO] ------------------------------------------------------------------------
[ERROR] Rule violated for bundle TestProj: instructions covered ratio is 0.4, but expected minimum is 0.7
[ERROR] Rule violated for bundle TestProj: lines covered ratio is 0.6, but expected minimum is 0.7
```
The plugin creates JaCoCo like HTML report by path `target/site/diffCoverage/html/index.html`

<img src="https://user-images.githubusercontent.com/8483470/86543421-fba86100-bf26-11ea-9549-98f801d0f2b9.png" width=500  alt="DiffCoverage HTML report"/>

<details>
  <summary>JaCoCo HTML report</summary> 
  <img src="https://user-images.githubusercontent.com/8483470/86543419-f3502600-bf26-11ea-873d-303c3a9d06dc.png" width=500 alt="JaCoCo HTML report"/>        
</details>
