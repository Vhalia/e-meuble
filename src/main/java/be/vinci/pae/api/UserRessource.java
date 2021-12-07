package be.vinci.pae.api;

import java.util.List;
import org.glassfish.jersey.server.ContainerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.User;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.filters.Admin;
import be.vinci.pae.filters.Authorize;
import be.vinci.pae.uc.UserUCC;
import be.vinci.pae.utils.Json;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Class which represents the resource for the user.
 */
@Path("/users")
@Singleton
public class UserRessource {

  private final ObjectMapper jsonMapper = new ObjectMapper();

  @Inject
  private UserUCC userUCC;

  /**
   * Route which allows to verify the validity of a token and it will retrieve all the data of the
   * user in order to update them in the front if needed.
   * 
   * @param request request body built in the authorization filter with the data of the user in the
   *        property "user".
   * @return the user.
   */
  @GET
  @Path("me")
  @Produces(MediaType.APPLICATION_JSON)
  @Authorize
  public Response me(@Context ContainerRequest request) {
    UserDTO currentUser = (UserDTO) request.getProperty("user");
    currentUser = Json.filterPublicJsonView(currentUser, UserDTO.class);
    ObjectNode node = jsonMapper.createObjectNode()
        .put("token", request.getProperty("token").toString()).putPOJO("user", currentUser);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();

  }

  /**
   * Route which allows to validate a role requested by the user when he registered.
   * 
   * @param user the user who requested a role.
   * @param roleRequested the role requested.
   * @return userModified the user modified by the query.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("validateRegister")
  @Admin
  public UserDTO validateRegister(User user, @QueryParam("roleRequested") String roleRequested) {
    user.setRole(roleRequested);
    UserDTO userModified = this.userUCC.validateRegister(user, roleRequested);
    return userModified;
  }

  /**
   * Route which allows to refuse the role requested by an user.
   * 
   * @param user the user who requested a role.
   * @return userModified the user modified by the query.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("refuseRegister")
  @Admin
  public UserDTO refuseRegister(User user) {
    UserDTO userModified = this.userUCC.refuseRegister(user);
    return userModified;
  }

  /**
   * Route which allows to retrieve all the users not validated.
   * 
   * @return a list of user not validated.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("usersNotValidated")
  @Admin
  public List<UserDTO> usersNotValidated() {
    return this.userUCC.getUsersNotValidated();
  }

  /**
   * Route which allows to get all options of a user.
   * 
   * @param id the id of a client
   * @return list of the furnitures with an option made by the client.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("furnitureOption/{id}")
  @Authorize
  public List<FurnitureDTO> furnituresWithAnOption(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no user with id " + id + " found").type(MediaType.TEXT_PLAIN).build());
    }
    return this.userUCC.getFurnituresWithAnOption(id);
  }

  /**
   * Route which allows to get an user thanks to his id.
   * 
   * @param id the id of the user.
   * @return the user with the id in path parameter.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  @Admin
  public UserDTO getUser(@PathParam("id") int id) {
    return userUCC.getUser(id);
  }

  /**
   * Route which allows to get all the furnitures sold by user.
   * 
   * @param id the id of a client.
   * @return list of the furnitures sold by the client.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("soldFurnitures/{id}")
  @Authorize
  public List<FurnitureDTO> soldFurnitures(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no user with id " + id + " found").type(MediaType.TEXT_PLAIN).build());
    }
    return this.userUCC.getSoldFurnitures(id);
  }

  /**
   * Route which allows to get all the furnitures bought by user.
   * 
   * @param id the id of a client.
   * @return list of the furnitures bought by the client.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("boughtFurnitures/{id}")
  @Authorize
  public List<FurnitureDTO> boughtFurnitures(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no user with id " + id + " found").type(MediaType.TEXT_PLAIN).build());
    }
    return this.userUCC.getBoughtFurnitures(id);
  }

  /**
   * Route which allows to get all users thanks to the word given.
   * 
   * @param word is the keyword for the research.
   * @return the user with the id in path parameter.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("research")
  @Admin
  public List<UserDTO> getUsersWithResearch(@QueryParam("word") String word) {
    return userUCC.getUsersByResearch(word);
  }

  /**
   * Route which give a list of keyword available.
   * 
   * @return a list of words.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tags")
  @Admin
  public List<String> getUserKeywords() {
    return userUCC.getTags();
  }
}
