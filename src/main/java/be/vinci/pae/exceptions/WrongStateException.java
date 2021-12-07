package be.vinci.pae.exceptions;

public class WrongStateException extends RuntimeException {
  private static final long serialVersionUID = -5837223620721095722L;

  /**
   * Unchecked exception which is use when trying to change the state of a furniture but the current
   * state don't allows it.
   */
  public WrongStateException() {
    super();
  }

  /**
   * Unchecked exception which is use when trying to change the state of a furniture but the current
   * state don't allows it.
   * 
   * @param message message to display.
   */
  public WrongStateException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is use when trying to change the state of a furniture but the current
   * state don't allows it.
   * 
   * @param cause the cause that caused the exception to be thrown.
   */
  public WrongStateException(Throwable cause) {
    super(cause);
  }

}
