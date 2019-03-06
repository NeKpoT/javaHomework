package com.example.cv2;

import com.example.cv2.testClasses.ClassWithOneClassDependency;
import com.example.cv2.testClasses.ClassWithOneInterfaceDependency;
import com.example.cv2.testClasses.ClassWithoutDependencies;
import com.example.cv2.testClasses.InterfaceImpl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;


public class TestInjector {

    @Test
    public void injectorShouldInitializeClassWithoutDependencies()
            throws Exception {
        Object object = Injector.initialize("com.example.cv2.testClasses.ClassWithoutDependencies", Collections.emptyList());
        assertTrue(object instanceof ClassWithoutDependencies);
    }

    @Test
    public void injectorShouldInitializeClassWithOneClassDependency()
            throws Exception {
        Object object = Injector.initialize(
                "com.example.cv2.testClasses.ClassWithOneClassDependency",
                Collections.singletonList("com.example.cv2.testClasses.ClassWithoutDependencies")
        );
        assertTrue(object instanceof ClassWithOneClassDependency);
        ClassWithOneClassDependency instance = (ClassWithOneClassDependency) object;
        assertTrue(instance.dependency != null);
    }

    @Test
    public void injectorShouldInitializeClassWithOneInterfaceDependency()
            throws Exception {
        Object object = Injector.initialize(
                "com.example.cv2.testClasses.ClassWithOneInterfaceDependency",
                Collections.singletonList("com.example.cv2.testClasses.InterfaceImpl")
        );
        assertTrue(object instanceof ClassWithOneInterfaceDependency);
        ClassWithOneInterfaceDependency instance = (ClassWithOneInterfaceDependency) object;
        assertTrue(instance.dependency instanceof InterfaceImpl);
    }
}