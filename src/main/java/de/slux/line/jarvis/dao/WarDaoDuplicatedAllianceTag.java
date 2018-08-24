/**
 * 
 */
package de.slux.line.jarvis.dao;

/**
 * @author slux
 *
 */
public class WarDaoDuplicatedAllianceTag extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public WarDaoDuplicatedAllianceTag() {
	}

	/**
	 * @param message
	 */
	public WarDaoDuplicatedAllianceTag(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public WarDaoDuplicatedAllianceTag(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WarDaoDuplicatedAllianceTag(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WarDaoDuplicatedAllianceTag(String message, Throwable cause, boolean enableSuppression,
	        boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
