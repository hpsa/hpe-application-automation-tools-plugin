package ngaclient;

/**
 * A runtime exception thrown when something was wrong with the request we want to send to NGA
 * @author Romulus Pa&#351;ca
 */
public class NgaRequestException extends RuntimeException {

	private static final long serialVersionUID = -4430281521213447064L;

	private final String errorCode;

	private final String errorResponse;

	public NgaRequestException(String errorCode, String errorMessage, String errorResponse) {
		super(errorMessage);
		this.errorCode = errorCode;
		this.errorResponse = errorResponse;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorResponse() {
		return errorResponse;
	}

}
