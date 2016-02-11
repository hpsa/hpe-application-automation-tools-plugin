package com.hp.nga.integrations.dto.pipelines;

import com.hp.nga.integrations.dto.DTOBase;
import com.hp.nga.integrations.dto.scm.SCMData;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sadea
 * Date: 29/07/15
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */

public interface BuildHistory extends DTOBase {

	class Build {
		private String status;
		private String number;
		private String time;
		private String startTime;
		private String duration;
		private SCMData scmData;
		private Set<SCMUser> culprits;


		Build(String status, String number, String time) {
			this.status = status;
			this.number = number;
			this.time = time;
		}

		public Build(String status, String number, String time, String startTime, String duration, SCMData scmData, Set<SCMUser> culprits) {
			this.status = status;
			this.number = number;
			this.time = time;
			this.startTime = startTime;
			this.duration = duration;
			this.scmData = scmData;
			this.culprits = culprits;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getNumber() {
			return number;
		}

		public void setNumber(String number) {
			this.number = number;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getDuration() {
			return duration;
		}

		public SCMData getScmData() {
			return scmData;
		}

		public Set<SCMUser> getCulprits() {
			return culprits;
		}
	}

	void addBuild(String status, String number, String time, String startTime, String duration, SCMData scmData, Set<SCMUser> culprits);

	void addLastSuccesfullBuild(String status, String number, String time, String startTime, String duration, SCMData scmData, Set<SCMUser> culprits);

	void addLastBuild(String status, String number, String time, String startTime, String duration, SCMData scmData, Set<SCMUser> culprits);

	Build getLastSuccesfullBuild();

	Build[] getBuilds();

	Build getLastBuild();

	class SCMUser {
		private String id;
		private String fullName;
		private String displayName;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
	}
}
