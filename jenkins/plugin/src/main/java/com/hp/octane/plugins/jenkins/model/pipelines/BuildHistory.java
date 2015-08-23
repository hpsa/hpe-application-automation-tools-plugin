package com.hp.octane.plugins.jenkins.model.pipelines;

import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import hudson.model.User;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sadea
 * Date: 29/07/15
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
@ExportedBean
public class BuildHistory {
    private ArrayList<Build> builds;
    private Build lastSuccesfullBuild;
    private Build lastBuild;

    @ExportedBean
    public static class Build {
        private String status;
        private String number;
        private String time;
        private String startTime;
        private String duration;
        private SCMData scmData;
        private Set<User> culprits;


        Build(String status, String number, String time) {
            this.status = status;
            this.number = number;
            this.time = time;
        }

        public Build(String status, String number, String time, String startTime, String duration,SCMData scmData ,Set<User> culprits) {
            this.status = status;
            this.number = number;
            this.time = time;
            this.startTime = startTime;
            this.duration = duration;
            this.scmData = scmData;
            this.culprits=culprits;
        }

        @Exported(inline = true)
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Exported(inline = true)
        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        @Exported(inline = true)
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        @Exported(inline = true)
        public String getStartTime() {
            return startTime;
        }

        @Exported(inline = true)
        public String getDuration() {
            return duration;
        }
        @Exported(inline = true)
        public SCMData getScmData() {
            return scmData;
        }
        @Exported(inline = true)
        public Set<User> getCulprits() {
            return culprits;
        }
    }

    public BuildHistory() {
        this.builds = new ArrayList<Build>();
        this.lastSuccesfullBuild = null;
        this.lastBuild = null;
    }

    public void addBuild(String status, String number, String time, String startTime, String duration,SCMData scmData , Set<User> culprits) {
        builds.add(new Build(status, number, time, startTime, duration,scmData,culprits));
    }

    public void addLastSuccesfullBuild(String status, String number, String time, String startTime, String duration,SCMData scmData , Set<User> culprits) {
        lastSuccesfullBuild = new Build(status, number, time, startTime, duration, scmData,culprits);
    }

    public void addLastBuild(String status, String number, String time, String startTime, String duration,SCMData scmData , Set<User> culprits) {
        lastBuild = new Build(status, number, time, startTime, duration,scmData,culprits);
    }


    @Exported(inline = true)
    public Build getLastSuccesfullBuild() {
        return lastSuccesfullBuild;
    }

    @Exported(inline = true)
    public Build[] getBuilds() {
        return builds.toArray(new Build[builds.size()]);
    }

    @Exported(inline = true)
    public Build getLastBuild() {
        return lastBuild;
    }
}
