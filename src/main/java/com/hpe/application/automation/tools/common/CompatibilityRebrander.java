package com.hpe.application.automation.tools.common;


import hudson.model.Items;
import hudson.model.Run;

import javax.annotation.Nonnull;
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
 * <li> Only the various Descriptors (e.g. BuildStepDescriptor) use this function
 * </ul>
 */
public class CompatibilityRebrander {
    private final static String COM_HPE = "com.hpe";
    private final static String COM_HP = "com.hp";
    private final static String COM_MICROFOCUS = "com.microfocus";
    private final static Logger LOG = Logger.getLogger(CompatibilityRebrander.class.getName());

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
    public static void addAliases(@Nonnull Class newClass) {
        String newClassName = newClass.toString().replaceFirst("class ", "");
        String oldHpeClassName = newClassName.replaceFirst(COM_MICROFOCUS, COM_HPE);
        String oldHpClassName = newClassName.replaceFirst(COM_MICROFOCUS, COM_HP);

        LOG.info("Starting the rebranding aliasing");
        addAliasesForSingleClass(newClass, oldHpClassName, COM_HP);
        addAliasesForSingleClass(newClass, oldHpeClassName, COM_HPE);
    }

    /**
     * addAliasesForSingleClass responsible for handling the rebranding for a single class
     */
    private static void addAliasesForSingleClass(@Nonnull Class newClass, String oldClassName, String beforeBrand) {
        handleReceivedWrongParameters(newClass, oldClassName, beforeBrand);
        invokeXstreamCompatibilityAlias(newClass, oldClassName);
    }

    /**
     * invokeXstreamCompatibilityAlias invokes the XSTREAM2 functions required for the rebranding
     */
    private static void invokeXstreamCompatibilityAlias(@Nonnull Class newClass, String oldClassName) {
        LOG.info(String.format("Adding alias from %s to %s", oldClassName, newClass));
        Items.XSTREAM2.addCompatibilityAlias(oldClassName, newClass);
        Run.XSTREAM2.addCompatibilityAlias(oldClassName, newClass);
    }

    /**
     * handleReceivedWrongParameters logs a warning when the passed newClass doesn't contain any of the package names
     * we trying to add alias to
     */
    private static void handleReceivedWrongParameters(@Nonnull Class newClass, String oldClassName, String beforeBrand) {
        if (!oldClassName.contains(beforeBrand))
            LOG.warning(String.format("The %s class name doesn't contain: %s class name", newClass.toString(), beforeBrand));
    }
}