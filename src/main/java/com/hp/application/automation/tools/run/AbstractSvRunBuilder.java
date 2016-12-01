package com.hp.application.automation.tools.run;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.application.automation.tools.model.AbstractSvRunModel;
import com.hp.application.automation.tools.model.SvServerSettingsModel;
import com.hp.application.automation.tools.model.SvServiceSelectionModel;
import com.hp.sv.jsvconfigurator.build.ProjectBuilder;
import com.hp.sv.jsvconfigurator.core.IProject;
import com.hp.sv.jsvconfigurator.core.IService;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommandExecutorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.CommunicatorException;
import com.hp.sv.jsvconfigurator.core.impl.exception.ProjectBuilderException;
import com.hp.sv.jsvconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.hp.sv.jsvconfigurator.serverclient.ICommandExecutor;
import com.hp.sv.jsvconfigurator.serverclient.impl.CommandExecutorFactory;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;

class ServiceInfo {
    private final String id;
    private final String name;

    public ServiceInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
        super(message);
    }
}

public abstract class AbstractSvRunBuilder<T extends AbstractSvRunModel> extends Builder {
    private static final Logger LOG = Logger.getLogger(AbstractSvRunBuilder.class.getName());

    protected final T model;

    protected AbstractSvRunBuilder(T model) {
        this.model = model;
    }

    protected static void verifyNotNull(Object value, String errorMessage) throws ConfigurationException {
        if (value == null) {
            throw new ConfigurationException(errorMessage);
        }
    }

    public T getModel() {
        return model;
    }

    public SvServiceSelectionModel getServiceSelection() {
        return model.getServiceSelection();
    }

    protected SvServerSettingsModel getSelectedServerSettings() throws ConfigurationException {
        SvServerSettingsModel[] servers = ((AbstractSvRunDescriptor) getDescriptor()).getServers();
        if (servers != null) {
            for (SvServerSettingsModel serverSettings : servers) {
                if (model.getServerName() != null && model.getServerName().equals(serverSettings.getName())) {
                    return serverSettings;
                }
            }
        }
        throw new ConfigurationException("Selected server configuration '" + model.getServerName() + "' does not exist.");
    }

    protected abstract boolean performImpl(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws Exception;

    protected ICommandExecutor createCommandExecutor() throws Exception {
        SvServerSettingsModel serverModel = getSelectedServerSettings();
        return new CommandExecutorFactory().createCommandExecutor(serverModel.getUrlObject(), serverModel.getCredentials());
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

        PrintStream logger = listener.getLogger();
        Date startDate = new Date();
        try {
            SvServerSettingsModel serverModel = getSelectedServerSettings();

            logger.printf("%nStarting %s for SV Server '%s' (%s as %s) on %s%n", getDescriptor().getDisplayName(),
                    serverModel.getName(), serverModel.getUrlObject(), serverModel.getUsername(), startDate);
            logConfig(logger, "    ");
            validateServiceSelection();
            return performImpl(build, launcher, listener);
        } catch (Exception e) {
            listener.fatalError(e.getMessage());
            LOG.log(Level.SEVERE, "Build failed: " + e.getMessage(), e);
        } finally {
            double duration = (new Date().getTime() - startDate.getTime()) / 1000.;
            logger.printf("Finished: %s in %.3f seconds%n%n", getDescriptor().getDisplayName(), duration);
        }
        return false;
    }

    protected void logConfig(PrintStream logger, String prefix) {
        SvServiceSelectionModel ss = model.getServiceSelection();
        switch (ss.getSelectionType()) {
            case SERVICE:
                logger.println(prefix + "Service name or id: " + ss.getService());
                break;
            case PROJECT:
                logger.println(prefix + "Project path: " + ss.getProjectPath());
                logger.println(prefix + "Project password: " + ((StringUtils.isNotBlank(ss.getProjectPassword())) ? "*****" : null));
                break;
            case ALL_DEPLOYED:
                logger.println(prefix + "All deployed services");
                break;
            case DEPLOY:
                logger.println(prefix + "Project path: " + ss.getProjectPath());
                logger.println(prefix + "Project password: " + ((StringUtils.isNotBlank(ss.getProjectPassword())) ? "*****" : null));
                logger.println(prefix + "Service name or id: " + ss.getService());
                break;
        }
        logger.println(prefix + "Force: " + model.isForce());
    }

    protected Iterable<ServiceInfo> getServiceList(boolean ignoreMissingServices, PrintStream logger) throws Exception {
        SvServiceSelectionModel s = getServiceSelection();
        ICommandExecutor exec = createCommandExecutor();

        ArrayList<ServiceInfo> res = new ArrayList<ServiceInfo>();

        switch (s.getSelectionType()) {
            case SERVICE:
                addServiceIfDeployed(s.getService(), res, ignoreMissingServices, exec, logger);
                break;
            case PROJECT:
                IProject project = loadProject();
                for (IService svc : project.getServices()) {
                    addServiceIfDeployed(svc.getId(), res, ignoreMissingServices, exec, logger);
                }
                break;
            case ALL_DEPLOYED:
                for (ServiceListAtom.ServiceEntry entry : exec.getServiceList(null).getEntries()) {
                    res.add(new ServiceInfo(entry.getId(), entry.getTitle()));
                }
            case DEPLOY:
                break;
        }
        return res;
    }

    private void addServiceIfDeployed(String service, ArrayList<ServiceInfo> results, boolean ignoreMissingServices, ICommandExecutor exec, PrintStream logger) throws CommunicatorException, CommandExecutorException {
        try {
            IService svc = exec.findService(service, null);
            results.add(new ServiceInfo(svc.getId(), svc.getName()));
        } catch (CommandExecutorException e) {
            if (!ignoreMissingServices) {
                throw e;
            }
            logger.printf("Service '%s' is not deployed, ignoring%n", service);
        }
    }

    protected IProject loadProject() throws ProjectBuilderException {
        SvServiceSelectionModel s = getServiceSelection();
        return new ProjectBuilder().buildProject(new File(s.getProjectPath()), s.getProjectPassword());
    }

    protected void validateServiceSelection() throws ConfigurationException {
        SvServiceSelectionModel s = getServiceSelection();
        switch (s.getSelectionType()) {
            case SERVICE:
                verifyNotNull(s.getService(), "Service name or id must not be empty if service selection by name or id set");
                break;
            case PROJECT:
                verifyNotNull(s.getProjectPath(), "Project path must not be empty if service selection by project is set");
                break;
            case ALL_DEPLOYED:
                break;
            case DEPLOY:
                verifyNotNull(s.getProjectPath(), "Project path must not be empty for deployment");
                break;
        }
    }
}
