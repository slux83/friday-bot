/**
 * 
 */
package de.slux.line.friday.dao.exception;

/**
 * @author slux
 */
public class SummonerNumberExceededException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SummonerNumberExceededException() {
	}

	/**
	 * @param message
	 */
	public SummonerNumberExceededException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SummonerNumberExceededException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SummonerNumberExceededException(String message, Throwable cause) {
		super(message, cause);
	}

}
