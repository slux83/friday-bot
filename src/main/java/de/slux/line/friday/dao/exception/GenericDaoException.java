/**
 * 
 */
package de.slux.line.friday.dao.exception;

/**
 * Just when something went wrong
 * 
 * @author slux
 */
public class GenericDaoException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public GenericDaoException() {
	}

	/**
	 * @param message
	 */
	public GenericDaoException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public GenericDaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GenericDaoException(String message, Throwable cause) {
		super(message, cause);
	}
}
