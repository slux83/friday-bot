/**
 * 
 */
package de.slux.line.friday.dao.exception;

/**
 * @author slux
 */
public class SummonerNumberExceededException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public SummonerNumberExceededException(String message) {
		super(message);
	}
}
