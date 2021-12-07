package be.vinci.pae.exceptions;

public class SomethingWentWrongException extends RuntimeException {

  private static final long serialVersionUID = -5543111745705273886L;

  /**
   * Unchecked exception which is use when something went wrong during the life time of the
   * application.
   */
  public SomethingWentWrongException() {
    super();
  }

  /**
   * Unchecked exception which is use when something went wrong during the life time of the
   * application.
   * 
   * @param message message to display.
   */
  public SomethingWentWrongException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is use when something went wrong during the life time of the
   * application.
   * 
   * @param cause the cause that caused the exception to be thrown.
   */
  public SomethingWentWrongException(Throwable cause) {
    super(cause);
  }

}
