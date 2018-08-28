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

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public SummonerNotFoundException(String message) {
		super(message);
	}

}
