package com.test.moduleB;

import org.junit.Test;

public class ClassModuleBTest {
    @Test
    public void test() {
        new ClassModuleB().sayHello(true);
        new ClassModuleB().sayHello(false);
    }
}
