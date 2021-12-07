package be.vinci.pae.exceptions;

public class UnauthorizedException extends RuntimeException {

  private static final long serialVersionUID = -8497609760648061569L;

  /**
   * Unchecked exception which is used when trying to do something without the authorization.
   */
  public UnauthorizedException() {
    super();
  }

  /**
   * Unchecked exception which is used when trying to do something without the authorization.
   * 
   * @param message message that explain why the exception has been thrown.
   */
  public UnauthorizedException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is used when trying to do something without the authorization.
   * 
   * @param message message that explain why the exception has been thrown.
   * @param cause the cause that caused the exception to be thrown.
   */
  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
