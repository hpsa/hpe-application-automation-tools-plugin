package com.microfocus.application.automation.tools.run;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.microfocus.application.automation.tools.model.AbstractSvRunModel;
import com.microfocus.application.automation.tools.model.SvServerSettingsModel;
import com.microfocus.application.automation.tools.model.SvServiceSelectionModel;
import com.microfocus.sv.svconfigurator.build.ProjectBuilder;
import com.microfocus.sv.svconfigurator.core.IProject;
import com.microfocus.sv.svconfigurator.core.IService;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommandExecutorException;
import com.microfocus.sv.svconfigurator.core.impl.exception.CommunicatorException;
import com.microfocus.sv.svconfigurator.core.impl.exception.ProjectBuilderException;
import com.microfocus.sv.svconfigurator.core.impl.jaxb.atom.ServiceListAtom;
import com.microfocus.sv.svconfigurator.serverclient.ICommandExecutor;
import com.microfocus.sv.svconfigurator.serverclient.impl.CommandExecutorFactory;
import hudson.FilePath;
import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;

public abstract class AbstractRemoteRunner<T extends AbstractSvRunModel> extends MasterToSlaveCallable<String, Exception> {
    protected T model;
    protected FilePath workspace;
    protected TaskListener listener;
    protected SvServerSettingsModel server;

    public AbstractRemoteRunner(TaskListener listener, T model, FilePath workspace, SvServerSettingsModel server) {
        this.listener = listener;
        this.model = model;
        this.workspace = workspace;
        this.server = server;
    }

    protected List<ServiceInfo> getServiceList(boolean ignoreMissingServices, PrintStream logger, FilePath workspace) throws Exception {
        SvServiceSelectionModel s = getServiceSelection();
        ICommandExecutor exec = createCommandExecutor();

        ArrayList<ServiceInfo> res = new ArrayList<>();

        switch (s.getSelectionType()) {
            case SERVICE:
                addServiceIfDeployed(s.getService(), res, ignoreMissingServices, exec, logger);
                break;
            case PROJECT:
                IProject project = loadProject(workspace);
                for (IService svc : project.getServices()) {
                    addServiceIfDeployed(svc.getId(), res, ignoreMissingServices, exec, logger);
                }
                break;
            case ALL_DEPLOYED:
                for (ServiceListAtom.ServiceEntry entry : exec.getServiceList(null).getEntries()) {
                    res.add(new ServiceInfo(entry.getId(), entry.getTitle()));
                }
                break;
            case DEPLOY:
                break;
        }
        return res;
    }

    protected IProject loadProject(FilePath workspace) throws ProjectBuilderException {
        SvServiceSelectionModel s = getServiceSelection();
        FilePath projectPath = workspace.child(s.getProjectPath());
        return new ProjectBuilder().buildProject(new File(projectPath.getRemote()), s.getProjectPassword());
    }

    public SvServiceSelectionModel getServiceSelection() {
        return model.getServiceSelection();
    }

    private void addServiceIfDeployed(String service, ArrayList<ServiceInfo> results, boolean ignoreMissingServices,
                                  ICommandExecutor exec, PrintStream logger) throws CommunicatorException, CommandExecutorException {
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

    protected ICommandExecutor createCommandExecutor() throws Exception {
        return new CommandExecutorFactory().createCommandExecutor(server.getUrlObject(), server.getCredentials());
    }
}
