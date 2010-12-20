package saere;

public class PrologException extends RuntimeException {

    private static final long serialVersionUID = -2598404594038748075L;

    public PrologException() {
	super();
    }

    public PrologException(String message, Throwable cause) {
	super(message, cause);
    }

    public PrologException(String message) {
	super(message);
    }

    public PrologException(Throwable cause) {
	super(cause);
    }

}
