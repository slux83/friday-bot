/**
 * 
 */
package de.slux.line.jarvis.dao;

/**
 * @author slux
 *
 */
public class WarDaoDuplicatedAllianceTagException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public WarDaoDuplicatedAllianceTagException() {
	}

	/**
	 * @param message
	 */
	public WarDaoDuplicatedAllianceTagException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public WarDaoDuplicatedAllianceTagException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WarDaoDuplicatedAllianceTagException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WarDaoDuplicatedAllianceTagException(String message, Throwable cause, boolean enableSuppression,
	        boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
