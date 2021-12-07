package be.vinci.pae.exceptions;

public class IncorrectDurationException extends RuntimeException {

  private static final long serialVersionUID = 798098204665502319L;

  /**
   * Unchecked exception which is use when the duration is incorrect.
   */
  public IncorrectDurationException() {
    super();
  }

  /**
   * Unchecked exception which is use when the duration is incorrect.
   * 
   * @param message message to display.
   */
  public IncorrectDurationException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is use when the duration is incorrect.
   * 
   * @param cause the cause that caused the exception to be thrown.
   */
  public IncorrectDurationException(Throwable cause) {
    super(cause);
  }
}
