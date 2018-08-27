/**
 * 
 */
package de.slux.line.friday.dao.exception;

/**
 * Just when something went wrong
 * 
 * @author slux
 */
public class SummonerNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SummonerNotFoundException() {
	}

	/**
	 * @param message
	 */
	public SummonerNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SummonerNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SummonerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
