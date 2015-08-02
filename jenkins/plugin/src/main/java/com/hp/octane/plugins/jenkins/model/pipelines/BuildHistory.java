package com.hp.octane.plugins.jenkins.model.pipelines;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;

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

    @ExportedBean
    public static class Build {
        private String status;
        private String number;
        private String time;


        Build(String status, String number,String time) {
            this.status = status;
            this.number = number;
            this.time = time;
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

    }

    public BuildHistory() {
        this.builds = new ArrayList<Build>();;
        this.lastSuccesfullBuild = null;
    }
    public void addBuild(String status, String number,String time) {
        builds.add(new Build(status, number,time));
    }
    public void addLastSuccesfullBuild(String status, String number,String time) {
        lastSuccesfullBuild = new Build(status, number,time);
    }


    @Exported(inline = true)
    public Build getLastSuccesfullBuild() {
        return lastSuccesfullBuild;
    }
    @Exported(inline = true)
    public Build[] getBuilds() {
        return builds.toArray(new Build[builds.size()]);
    }
}
