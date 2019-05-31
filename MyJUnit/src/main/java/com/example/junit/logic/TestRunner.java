package com.example.junit.logic;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Test runner can run test methods in all classes
 * in .class or .jar files in provided directory.
 *
 * All classes are loaded prior to execution.
 *
 * Any methods are executed only in classes that contain at least
 * one method annotated with {@code @Test}.
 *
 * Each class's methods are executed in the same thread, but
 * generally <code>Runtime.getRuntime().availableProcessors()</code>
 * threads used.
 *
 * It continues to execute methods even if any of previous
 * executed methods failed.
 */
public class TestRunner {
    private final ExecutorService testExecutor;
    private final List<TestLogger> loggers = new LinkedList<>();
    private final List<Class<?>> classes = new LinkedList<>();

    /**
     * Constructs a new <code>TestRunner</code>
     */
    public TestRunner() {
        testExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param file   a single file or a directory to search for them.
     * @param output where to write test logs
     * @throws FileNotFoundException if provided file does not exists

     */
    public void test(File file, PrintStream output) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // should not happen
        }

        int pathLength = file.getAbsoluteFile().toPath().getNameCount();
        if (file.isFile()) {
            loadFile(classLoader, file, pathLength);
        }

        if (file.isDirectory()) {
            loadDirectory(classLoader, file, pathLength);
        }

        var futures = new LinkedList<Future>();
        for (Class<?> clazz : classes) {
            futures.add(testClass(clazz));
        }

        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                testExecutor.shutdownNow();
                return;
            } catch (ExecutionException e) {
                throw new RuntimeException(e); // should not happen
            }
        }

        for (TestLogger logger : loggers) {
            logger.writeToOutput(output);
        }
    }

    private Future testClass(Class<?> classToTest) {
        var logger = new TestLogger();
        loggers.add(logger);
        return testExecutor.submit(new TestTask(classToTest, logger));
    }

    private void loadDirectory(URLClassLoader classLoader, File directory, int stripFrom) {
        for (File f : directory.listFiles()) {
            if (f.isFile()) {
                loadFile(classLoader, f, stripFrom);
            }
            if (f.isDirectory()) {
                loadDirectory(classLoader, f, stripFrom);
            }
        }
    }

    private void loadFile(URLClassLoader classLoader, File file, int stripFrom) {
        Path filePath = file.getAbsoluteFile().toPath();
        String fileWithPackage = filePath.subpath(stripFrom, filePath.getNameCount()).toString();

        if (fileWithPackage.endsWith(".class") || fileWithPackage.endsWith(".jar")) {
            String className = fileWithPackage.substring(0, fileWithPackage.lastIndexOf('.')).replace(File.separatorChar, '.');
            try {
                classes.add(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                // should not happen
            }
        }
    }
}
