package be.vinci.pae.filters;

import java.io.IOException;
import com.auth0.jwt.interfaces.DecodedJWT;
import be.vinci.pae.domain.User;
import be.vinci.pae.uc.UserUCC;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

@Singleton
@Provider
@Admin
public class AdminFilter implements ContainerRequestFilter {

  @Inject
  private UserUCC userUcc;

  @Override
  @Authorize
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String token = requestContext.getHeaderString("Authorization");
    DecodedJWT decodedToken = FilterUtils.checkToken(requestContext, token);
    User u = (User) userUcc.getUser(decodedToken.getClaim("userId").asInt());
    if (!u.getRole().equals("ADM") || !u.isRegistrationValidated()) {
      throw new WebApplicationException(Response.status(Status.UNAUTHORIZED)
          .entity("Error: You must be an administrator to access this ressource").type("text/plain")
          .build());
    }
    requestContext.setProperty("user", u);
  }

}
