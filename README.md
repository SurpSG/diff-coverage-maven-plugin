# diff-coverage-maven-plugin
![CI](https://github.com/SurpSG/diff-coverage-maven-plugin/workflows/CI/badge.svg)
[![codecov](https://codecov.io/gh/SurpSG/diff-coverage-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/SurpSG/diff-coverage-maven-plugin)

## Install plugin
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.sgnat</groupId>
            <artifactId>diff-coverage-maven-plugin</artifactId>
            <version>0.0.1</version>
            <configuration>
                <diffSource>
                    <file>path/to/diffFile</file>
                    <url>http://url.com</url>
                </diffSource>
                <violations>
                    <failOnViolation>true</failOnViolation>
                    <minLines>0.7</minLines>
                    <minBranches>0.7</minBranches>
                    <minInstructions>0.7</minInstructions>
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
