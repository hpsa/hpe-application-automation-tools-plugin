package com.hpe.application.automation.tools.common;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.model.Run;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;


/**
 * CompatibilityRebrander is an interface for all related to the company rebranding phase.
 * <p>
 * This process is required because our serialized data includes the old package class name.
 * A measure must be taken in order to maintain backward compatibility.
 * <p>
 * Important note to mention is this class will only work after:
 * <ul>
 * <li> Package path is renamed from the old brand to the new one
 * </ul>
 */
public class CompatibilityRebrander {
    private static final String COM_HPE = "com.hpe";
    private static final String COM_HP = "com.hp";
    private static final String COM_MICROFOCUS = "com.microfocus";
    private static final String PACKAGE_NAME = "com.microfocus.application.automation.tools";
    private static final Logger LOG = Logger.getLogger(CompatibilityRebrander.class.getName());

    private CompatibilityRebrander() {}

    /**
     * This function hooks to the Jenkins milestone when the plugins are prepared to load
     * And adds aliases for all package found classes
     */
    @Initializer(before = InitMilestone.PLUGINS_PREPARED)
    public static void addAliasesToAllClasses() {
        LOG.info("Adding alias for in old package names to add backward compatibility");
        try {
            Class[] classes = getClasses(PACKAGE_NAME);
            for (Class c: classes) {
                addAliases(c);
            }
        }
        catch(ClassNotFoundException | IOException e) {
            LOG.warning(e.getMessage());
        }
    }

    /**
     * addAliases is the actual function who does the rebranding part for all the old package names
     * <p>
     * Items.XSTREAM2.addCompatibilityAlias is for serializing project configurations.
     * Run.XSTREAM2.addCompatibilityAlias is for serializing builds and its associated Actions.
     *
     * @param newClass the Descriptor class we want to add alias for
     * @see hudson.model.Items#XSTREAM2
     * @see hudson.model.Run#XSTREAM2
     * @since 5.5
     */
    private static void addAliases(@Nonnull Class newClass) {
        String newClassName = newClass.toString().replaceFirst("class ", "");
        String oldHpeClassName = newClassName.replaceFirst(COM_MICROFOCUS, COM_HPE);
        String oldHpClassName = newClassName.replaceFirst(COM_MICROFOCUS, COM_HP);

        invokeXstreamCompatibilityAlias(newClass, oldHpClassName);
        invokeXstreamCompatibilityAlias(newClass, oldHpeClassName);
    }

    /**
     * invokeXstreamCompatibilityAlias invokes the XSTREAM2 functions required for the rebranding
     */
    private static void invokeXstreamCompatibilityAlias(@Nonnull Class newClass, String oldClassName) {
        Items.XSTREAM2.addCompatibilityAlias(oldClassName, newClass);
        Run.XSTREAM2.addCompatibilityAlias(oldClassName, newClass);
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List directories = getDirectories(resources);
        ArrayList classes = getClassesArrayList(directories, packageName);

        return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    private static ArrayList getClassesArrayList(List directories, String packageName) throws ClassNotFoundException, IOException {
        ArrayList classes = new ArrayList();

        for (Object directory : directories) {
            classes.addAll(findClasses((File)directory, packageName));
        }

        return classes;
    }

    private static List getDirectories(Enumeration resources) {
        List directories = new ArrayList();

        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            directories.add(new File(resource.getFile()));
        }

        return directories;
    }

    /**
     * The context that is supplied was Jenkins and the project itself, it required the following workaround.
     * @return ClassLoader that is related to the project context
     */
    private static ClassLoader getContextClassLoader() {
        Thread thread = Thread.currentThread();
        ClassLoader tempClassLoader = thread.getContextClassLoader();
        assert tempClassLoader != null;
        thread.setContextClassLoader(CompatibilityRebrander.class.getClassLoader());
        return thread.getContextClassLoader();
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();

        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));

            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }
}

