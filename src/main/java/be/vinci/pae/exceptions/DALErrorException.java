package be.vinci.pae.exceptions;

public class DALErrorException extends RuntimeException {

  private static final long serialVersionUID = -2704622207007877869L;

  /**
   * Unchecked exception which is use when an error occurred in the data access layer.
   */
  public DALErrorException() {
    super();
  }

  /**
   * Unchecked exception which is use when an error occurred in the data access layer.
   * 
   * @param message message message to display.
   */
  public DALErrorException(String message) {
    super(message);
  }

  /**
   * Unchecked exception which is use when an error occurred in the data access layer.
   * 
   * @param cause cause the cause that caused the exception to be thrown.
   */
  public DALErrorException(Throwable cause) {
    super(cause);
  }

}
