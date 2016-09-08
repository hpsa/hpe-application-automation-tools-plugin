package ngaclient;

import static ngaclient.JsonUtils.newArray;
import static ngaclient.JsonUtils.newObject;

import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author Romulus Pa&#351;ca
 * 
 * A data structure for holding a build related information
 */

public class BuildInfo {

	/**
	 * The possiblr states of a build
	 * @author Romulus Pa&#351;ca
	 */
	public enum BuildStatus {
		UNAVAILABLE, RUNNING, FINISHED;
	}

	/*
	 * The possible results of a build
	 */
	public enum BuildResult {
		UNAVAILABLE, UNSTABLE, ABORTED, FAILURE, SUCCESS;
	}

	private String serverCiId;
	private JobInfo jobInfo;
	private String buildCiId;
	private String buildName; // optional

	private BuildStatus status = BuildStatus.FINISHED; // optional
	private BuildResult result;
	private Instant startTime = Instant.now();
	private Duration duration;

	private BuildCause[] causes;

	/**
	 * Populates the existing JSON node with the build information
	 * @param node - a root JSON object which is going to be populate with build information
	 * @return A JSON object which respects the format require by NGA REST API. In the current implementation will
	 * be the same node with the one passed as parameter.
	 */
	public ObjectNode asJson(ObjectNode node) {
		node.put("serverCiId", serverCiId);
		node.put("jobCiId", jobInfo.getId());
		node.put("buildCiId", buildCiId);
		node.put("buildName", buildName);
		node.put("status", status.toString().toLowerCase());
		node.put("result", result.toString().toLowerCase());
		node.put("startTime", startTime.toEpochMilli());
		node.put("duration", duration.toMillis());
		if (causes != null && causes.length > 0) {
			ArrayNode causesArray = newArray();
			for (BuildCause cause : causes) {
				ObjectNode causeNode = newObject();
				causeNode.put("jobCiId", cause.getJobCid());
				causeNode.put("buildCiId", cause.getBuildCid());
				causesArray.add(causeNode);
			}
			node.set("causes", causesArray);
		}
		return node;
	}

	/**
	 * Check is his build information is valid, so it will be accepted by NGA via the REST API
	 * @throws IllegalStateException
	 */
	public void validate() throws IllegalStateException {
		if (serverCiId == null || serverCiId.trim().length() == 0) {
			throw new IllegalStateException("Unspecified serverCiId");
		}
		if (jobInfo == null) {
			throw new IllegalStateException("Unspecified");
		}
		String jobCiId = jobInfo.getId();
		if (jobCiId == null || jobCiId.trim().length() == 0) {
			throw new IllegalStateException("Unspecified jobCiId");
		}
		if (buildCiId == null || buildCiId.trim().length() == 0) {
			throw new IllegalStateException("Unspecified buildCiId");
		}
		if (buildName == null || buildName.trim().length() == 0) {
			buildName = buildCiId;
		}
		if (status == null) {
			status = BuildStatus.FINISHED;
		}
		if (result == null) {
			throw new IllegalStateException("Build Result unspecified");
		}
		if (startTime == null) {
			throw new IllegalStateException("Build StartTime unspecified");
		}
		if (duration == null) {
			throw new IllegalStateException("Build Duration unspecified");
		}
	}

	public String getServerCiId() {
		return serverCiId;
	}

	public void setServerCiId(String serverCiId) {
		this.serverCiId = serverCiId;
	}

	public JobInfo getJob() {
		return jobInfo;
	}

	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}

	public String getBuildCiId() {
		return buildCiId;
	}

	public void setBuildCiId(String buildCiId) {
		this.buildCiId = buildCiId;
	}

	public BuildStatus getStatus() {
		return status;
	}

	public void setStatus(BuildStatus status) {
		this.status = status;
	}

	public BuildResult getResult() {
		return result;
	}

	public void setResult(BuildResult result) {
		this.result = result;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public String getBuildName() {
		return buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public BuildCause[] getCauses() {
		return causes;
	}

	public void setCauses(BuildCause[] causes) {
		this.causes = causes;
	}

}
