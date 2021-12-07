package be.vinci.pae.exceptions;

public class AlreadyCancelException extends RuntimeException {

  private static final long serialVersionUID = 4106610192046320724L;

  /**
   * Unchecked exception which is use when the is cancel attribute is already on.
   */
  public AlreadyCancelException() {
    super();
  }

  /**
   * Unchecked exception which is use when the is cancel attribute is already on.
   * 
   * @param message message to display.
   */
  public AlreadyCancelException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is use when the is cancel attribute is already on.
   * 
   * @param cause the cause that caused the exception to be thrown.
   */
  public AlreadyCancelException(Throwable cause) {
    super(cause);
  }

}
