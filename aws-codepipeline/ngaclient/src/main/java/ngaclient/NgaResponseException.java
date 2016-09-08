package ngaclient;

/**
 * When an NGA REST API request will be answered with an error then that error will be wrapped on this type of exception
 * @author Romulus Pa&#351;ca
 *
 */
public class NgaResponseException extends RuntimeException {

	private static final long serialVersionUID = -7070964444495309541L;

	private final String response;

	public NgaResponseException(String message, String response) {
		super(message);
		this.response = response;
	}

	public String getResponse() {
		return response;
	}
}
