/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package jdk.internal.jrtfs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;

import jdk.internal.jimage.ImageReader;
import jdk.internal.jimage.ImageReader.Node;

/**
 * @implNote This class needs to maintain JDK 8 source compatibility.
 *
 * It is used internally in the JDK to implement jimage/jrtfs access,
 * but also compiled and delivered as part of the jrtfs.jar to support access
 * to the jimage file provided by the shipped JDK by tools running on JDK 8.
 */
abstract class SystemImage {

    abstract Node findNode(String path) throws IOException;
    abstract byte[] getResource(Node node) throws IOException;
    abstract void close() throws IOException;

    static SystemImage open() throws IOException {
        if (modulesImageExists) {
            // open a .jimage and build directory structure
            final ImageReader image = ImageReader.open(moduleImageFile);
            image.getRootDirectory();
            return new SystemImage() {
                @Override
                Node findNode(String path) throws IOException {
                    return image.findNode(path);
                }
                @Override
                byte[] getResource(Node node) throws IOException {
                    return image.getResource(node);
                }
                @Override
                void close() throws IOException {
                    image.close();
                }
            };
        }
        if (Files.notExists(explodedModulesDir))
            throw new FileSystemNotFoundException(explodedModulesDir.toString());
        return new ExplodedImage(explodedModulesDir);
    }

    static final String RUNTIME_HOME;
    // "modules" jimage file Path
    final static Path moduleImageFile;
    // "modules" jimage exists or not?
    final static boolean modulesImageExists;
    // <JAVA_HOME>/modules directory Path
    static final Path explodedModulesDir;

    static {
        PrivilegedAction<String> pa = SystemImage::findHome;
        RUNTIME_HOME = AccessController.doPrivileged(pa);

        FileSystem fs = FileSystems.getDefault();
        moduleImageFile = fs.getPath(RUNTIME_HOME, "lib", "modules");
        explodedModulesDir = fs.getPath(RUNTIME_HOME, "modules");

        modulesImageExists = AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return Files.isRegularFile(moduleImageFile);
                }
            });
    }

    /**
     * Returns the appropriate JDK home for this usage of the FileSystemProvider.
     * When the CodeSource is null (null loader) then jrt:/ is the current runtime,
     * otherwise the JDK home is located relative to jrt-fs.jar.
     */
    private static String findHome() {
        CodeSource cs = SystemImage.class.getProtectionDomain().getCodeSource();
        if (cs == null)
            return System.getProperty("java.home");

        // assume loaded from $TARGETJDK/lib/jrt-fs.jar
        URL url = cs.getLocation();
        if (!url.getProtocol().equalsIgnoreCase("file"))
            throw new InternalError(url + " loaded in unexpected way");
        try {
            Path lib = Paths.get(url.toURI()).getParent();
            if (!lib.getFileName().toString().equals("lib"))
                throw new InternalError(url + " unexpected path");

            return lib.getParent().toString();
        } catch (URISyntaxException e) {
            throw new InternalError(e);
        }
    }
}
