package ngaclient;

/**
 * 
 * @author Romulus Pa&#351;ca
 * 
 * A simple data structure for holding a build cause
 */
public class BuildCause {
	private String jobCid;
	private String buildCid;

	public BuildCause() {

	}

	public BuildCause(String jobCid, String buildCid) {
		this.jobCid = jobCid;
		this.buildCid = buildCid;
	}

	public String getJobCid() {
		return jobCid;
	}

	public void setJobCid(String jobCid) {
		this.jobCid = jobCid;
	}

	public String getBuildCid() {
		return buildCid;
	}

	public void setBuildCid(String buildCid) {
		this.buildCid = buildCid;
	}

	@Override
	public String toString() {
		return "BuildCause [jobCid=" + jobCid + ", buildCid=" + buildCid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (buildCid == null ? 0 : buildCid.hashCode());
		result = prime * result + (jobCid == null ? 0 : jobCid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BuildCause other = (BuildCause) obj;
		if (buildCid == null) {
			if (other.buildCid != null) {
				return false;
			}
		} else if (!buildCid.equals(other.buildCid)) {
			return false;
		}
		if (jobCid == null) {
			if (other.jobCid != null) {
				return false;
			}
		} else if (!jobCid.equals(other.jobCid)) {
			return false;
		}
		return true;
	}

}
