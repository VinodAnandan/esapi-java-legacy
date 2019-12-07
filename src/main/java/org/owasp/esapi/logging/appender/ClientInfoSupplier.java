package org.owasp.esapi.logging.appender;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.User;

/**
 * Supplier which can provide a String representing the client-side connection
 * information.
 */
public class ClientInfoSupplier implements Supplier<String> {
	/** Default UserName string if the Authenticated user is null.*/
	private static final String DEFAULT_USERNAME = "#ANONYMOUS#";
	/** Default Last Host string if the Authenticated user is null.*/
	private static final String DEFAULT_LAST_HOST = "#UNKNOWN_HOST#";
	/** Session Attribute containing the ESAPI Session id. */
	private static final String ESAPI_SESSION_ATTR = "ESAPI_SESSION";
	/**
	 * Minimum value for generating a random session value if one is not defined.
	 */
	private static final int ESAPI_SESSION_RAND_MIN = 0;
	/**
	 * Maximum value for generating a random session value if one is not defined.
	 */
	private static final int ESAPI_SESSION_RAND_MAX = 1000000;

	/** Format for supplier output. */
	private static final String USER_INFO_FORMAT = "%s:%s@%s"; // USER_NAME, SID, USER_HOST_ADDRESS

	/** Whether to log the user info from this instance. */
	private boolean logUserInfo = true;

	@Override
	public String get() {
		String userInfo = "";
	
		if (logUserInfo) {
			HttpServletRequest request = ESAPI.currentRequest();
			// create a random session number for the user to represent the user's
			// 'session', if it doesn't exist already
			String sid = "";
			if (request != null) {
				HttpSession session = request.getSession(false);
				if (session != null) {
					sid = (String) session.getAttribute(ESAPI_SESSION_ATTR);
					// if there is no session ID for the user yet, we create one and store it in the
					// user's session
					if (sid == null) {
						sid = "" + ESAPI.randomizer().getRandomInteger(ESAPI_SESSION_RAND_MIN, ESAPI_SESSION_RAND_MAX);
						session.setAttribute(ESAPI_SESSION_ATTR, sid);
					}
				}
			}
			// log user information - username:session@ipaddr
			User user = ESAPI.authenticator().getCurrentUser();
			if (user == null) {
				userInfo = String.format(USER_INFO_FORMAT, DEFAULT_USERNAME, sid, DEFAULT_LAST_HOST);
			} else {
				userInfo = String.format(USER_INFO_FORMAT, user.getAccountName(), sid, user.getLastHostAddress());
			}
		}
		return userInfo;
	}

	/**
	 * Specify whether the instance should record the client info.
	 * 
	 * @param log {@code true} to record
	 */
	public void setLogUserInfo(boolean log) {
		this.logUserInfo = log;
	}

}
