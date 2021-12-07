package be.vinci.pae.filters;

import java.io.IOException;
import com.auth0.jwt.interfaces.DecodedJWT;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.uc.UserUCC;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Singleton
@Provider
@Authorize
public class AuthorizationFilter implements ContainerRequestFilter {

  @Inject
  private UserUCC userUcc;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String token = requestContext.getHeaderString("Authorization");
    DecodedJWT decodedToken = FilterUtils.checkToken(requestContext, token);
    requestContext.setProperty("user", userUcc.getUser(decodedToken.getClaim("userId").asInt()));
    UserDTO u = userUcc.getUser(decodedToken.getClaim("userId").asInt());
    requestContext.setProperty("user", u);
    requestContext.setProperty("token", token);
    requestContext.setProperty("isAdmin", u.getRole().equals("ADM"));
  }

}
