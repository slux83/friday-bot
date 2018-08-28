/**
 * 
 */
package de.slux.line.friday.dao.exception;

/**
 * @author adfazio
 *
 */
public class WarDaoUnregisteredException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public WarDaoUnregisteredException(String message) {
		super(message);
	}
}