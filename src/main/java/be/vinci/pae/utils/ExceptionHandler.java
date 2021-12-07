package be.vinci.pae.utils;

import org.apache.log4j.Logger;
import be.vinci.pae.exceptions.UnauthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

  static Logger log = Logger.getLogger(ExceptionHandler.class.getName());

  @Override
  public Response toResponse(Throwable exception) {
    exception.printStackTrace();
    log.error(exception.toString() + ": " + getLinesStackTrace(5, exception));
    return Response.status(getStatusCode(exception)).entity(exception.toString()).build();
  }

  private int getStatusCode(Throwable exception) {
    if (exception instanceof WebApplicationException) {
      return ((WebApplicationException) exception).getResponse().getStatus();
    } else if (exception instanceof IllegalArgumentException) {
      return Response.Status.BAD_REQUEST.getStatusCode();
    } else if (exception instanceof UnauthorizedException) {
      return Response.Status.UNAUTHORIZED.getStatusCode();
    }
    return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  }

  private String getLinesStackTrace(int nbrStackTrace, Throwable exception) {
    String stackTrace = "";
    if (nbrStackTrace > exception.getStackTrace().length) {
      nbrStackTrace = exception.getStackTrace().length;
    }
    for (int i = 0; i < nbrStackTrace; i++) {
      stackTrace += "\n\tat: " + exception.getStackTrace()[i].toString();
    }
    return stackTrace;
  }
}
