package de.slux.line.friday.dao.exception;

/**
 * @author Slux
 */
public class DuplicatedNodeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -4124983374245477789L;

    /**
     * @param message
     * @param cause
     */
    public DuplicatedNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public DuplicatedNodeException(String message) {
        super(message);
    }

}
