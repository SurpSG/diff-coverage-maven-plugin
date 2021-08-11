package com.test.moduleA;

import org.junit.Test;

public class ClassModuleATest {
    @Test public void test() {
        new ClassModuleA().sayHello(true);
        new ClassModuleA().sayHello(false);
    }
}
