/**
 *
 */
package de.slux.line.friday.dao.exception;

/**
 * @author slux
 *
 */
public class WarDaoDuplicatedAllianceTagException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public WarDaoDuplicatedAllianceTagException(String message) {
        super(message);
    }
}
