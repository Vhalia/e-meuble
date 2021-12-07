package be.vinci.pae.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.apache.commons.text.StringEscapeUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.uc.UserUCC;
import be.vinci.pae.utils.Config;
import be.vinci.pae.utils.Json;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Class which represents the resource for the authentication of users.
 */
@Path("/authentication")
@Singleton
public class AuthenticationRessource {

  private final ObjectMapper jsonMapper = new ObjectMapper();
  private final Algorithm jwtAlgorithm = Algorithm.HMAC256(Config.getProperty("JWTSecret"));
  private final Date tokenLifeTime = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));

  @Inject
  private UserUCC userUCC;

  /**
   * Route to the login that verified userName and password entered. Create a JWT Token related to
   * the user who wants to connect.
   * 
   * @param json Contains data from the form.
   * @return A Response(Status).
   * 
   */
  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(JsonNode json) {
    // Get and check credentials
    if (!json.hasNonNull("userName") || !json.hasNonNull("password")) {
      return Response.status(Status.UNAUTHORIZED).entity("Error: user name and password needed")
          .type(MediaType.TEXT_PLAIN).build();
    }
    String userName = StringEscapeUtils.escapeHtml4(json.get("userName").asText());
    String password = json.get("password").asText();

    UserDTO user = null;
    try {
      // Try to login
      user = this.userUCC.login(userName, password);
    } catch (UnauthorizedException e) {
      return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
          .build();
    }

    user = Json.filterPublicJsonView(user, UserDTO.class);

    // create token
    String token;
    boolean isAdmin = false;
    boolean isAntiquarian = false;
    if (user.getRole().equals("ADM") && user.isRegistrationValidated()) {
      isAdmin = true;
    } else if (user.getRole().equals("ANT") && user.isRegistrationValidated()) {
      isAntiquarian = true;
    }
    try {
      token = JWT.create().withIssuer("2D2L").withExpiresAt(tokenLifeTime)
          .withClaim("userId", user.getId()).withClaim("isAdmin", isAdmin)
          .withClaim("isAntiquarian", isAntiquarian).sign(this.jwtAlgorithm);
    } catch (Exception e) {
      throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Error: Unable to create token").type(MediaType.TEXT_PLAIN).build());
    }

    // create response
    ObjectNode node = jsonMapper.createObjectNode().put("token", token).putPOJO("user", user);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();

  }

  /**
   * Route to the register that verified all of the user data entered. Create a JWT Token related to
   * the user who wants to connect.
   * 
   * @return A Response(Status).
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("register")
  public Response register(UserDTO newUser) {
    try {
      newUser = this.userUCC.register(newUser);
    } catch (UnauthorizedException e) {
      return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN)
          .build();
    }

    Json.filterPublicJsonView(newUser, UserDTO.class);

    // create token
    String token;

    try {
      token = JWT.create().withIssuer("2D2L").withExpiresAt(tokenLifeTime)
          .withClaim("userId", newUser.getId()).withClaim("isAdmin", false)
          .withClaim("isAntiquarian", false).sign(this.jwtAlgorithm);
    } catch (Exception e) {
      throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Error: Unable to create token").type(MediaType.TEXT_PLAIN).build());
    }

    // create response
    ObjectNode node = jsonMapper.createObjectNode().put("token", token).putPOJO("user", newUser);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }
}
