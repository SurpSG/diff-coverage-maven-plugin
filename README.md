# diff-coverage-maven-plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.sgnat</groupId>
            <artifactId>diff-coverage-maven-plugin</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <executions>
                <execution>
                    <configuration>
                        <diffSource></diffSource>
                        <violations></violations>
                    </configuration>
                    <goals>
                        <goal>diff-coverage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
