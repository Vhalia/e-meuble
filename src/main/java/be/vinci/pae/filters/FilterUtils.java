package be.vinci.pae.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import be.vinci.pae.utils.Config;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class FilterUtils {

  private static final Algorithm jwtAlgorithm = Algorithm.HMAC256(Config.getProperty("JWTSecret"));
  private static final JWTVerifier jwtVerifier =
      JWT.require(jwtAlgorithm).withIssuer("2D2L").build();

  /**
   * Allows to check if a token is valid.
   * 
   * @param requestContext the context of the request generally send by a filter.
   * @param token the token to verify.
   * @return the decoded token.
   */
  public static DecodedJWT checkToken(ContainerRequestContext requestContext, String token) {
    DecodedJWT decodedToken = null;
    if (token == null) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
          .entity("Error: A token is needed to access this resource").build());
    } else {
      try {
        decodedToken = jwtVerifier.verify(token);
      } catch (Exception e) {
        throw new WebApplicationException(Response.status(Status.UNAUTHORIZED)
            .entity("Error: Malformed token : " + e.getMessage()).type("text/plain").build());
      }
    }
    return decodedToken;

  }
}
