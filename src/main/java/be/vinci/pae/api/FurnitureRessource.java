package be.vinci.pae.api;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ContainerRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.filters.Admin;
import be.vinci.pae.filters.Authorize;
import be.vinci.pae.uc.FurnitureUCC;
import be.vinci.pae.utils.Config;
import be.vinci.pae.utils.Json;
import be.vinci.pae.utils.VariableChecker;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
@Path("/furnitures")
@Singleton
public class FurnitureRessource {

  @Inject
  private FurnitureUCC furnitureUCC;

  @Inject
  private DomainFactory domainFactory;

  private final ObjectMapper jsonMapper = new ObjectMapper();


  /**
   * Route to add a type.
   * 
   * @param type the libelle.
   * @return the type.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("addType")
  @Admin
  public Response addType(String type) {
    if (type == null || type == "") {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: empty String").type(MediaType.TEXT_PLAIN).build());
    }

    int id = furnitureUCC.addType(type);
    ObjectNode node = jsonMapper.createObjectNode().put("libelle", type).put("id", id);
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Route to change the state of a furniture from PROPO (proposé) to ENMAG (en magasin) or ENRES
   * (en restauration).
   * 
   * @param f the furniture.
   * @param nextState the new state (ENMAG (en magasin) or ENRES (en restauration)).
   * @return the modified furniture.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("fixPurchasePrice")
  @Admin
  public FurnitureDTO fixPurchasePrice(FurnitureDTO f, @QueryParam("nextState") String nextState) {
    // Get and check informations
    if (f.getId() <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no furniture with id " + f.getId() + " found").type(MediaType.TEXT_PLAIN)
          .build());
    }
    if (!VariableChecker.checkStringNotEmpty(nextState)) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: empty query parameter found").type(MediaType.TEXT_PLAIN).build());
    }
    f = this.furnitureUCC.fixPurchasePrice(f.getId(), f.getPurchasePrice(), nextState,
        f.getDateCarryFromClient());
    f = Json.filterPublicJsonView(f, FurnitureDTO.class);

    return f;
  }

  /**
   * Route to get a furniture by its id.
   * 
   * @param id the id of the furniture we want to get.
   * @return the furniture we want to get.
   */
  @GET
  @Path("{id}")
  @Authorize
  @Produces(MediaType.APPLICATION_JSON)
  public FurnitureDTO getFurniture(@PathParam("id") int id, @Context ContainerRequest request) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: invalid furniture id").type(MediaType.TEXT_PLAIN).build());
    }
    FurnitureDTO furniture =
        this.furnitureUCC.getFurniture(id, (UserDTO) request.getProperty("user"));
    return furniture;
  }

  /**
   * Change the status of a given furniture from ENMAG or ENRES to ENVEN.
   * 
   * @param f the furniture to change.
   * @param nextState is the new status of the furniture here ENVEN.
   * @return the new furniture.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("onSale")
  @Admin
  public FurnitureDTO fixSellPrice(FurnitureDTO f, @QueryParam("nextState") String nextState) {
    if (f.getId() <= 0 || nextState == null) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error : null parameters found").type(MediaType.TEXT_PLAIN).build());
    }
    f = this.furnitureUCC.fixSellPrice(f.getId(), f.getSellPrice(), f.getSpecialPrice(), nextState);
    return f;
  }

  /**
   * Route to change the state of a furniture from ENRES to ENMAG.
   * 
   * @param id the id of the furniture to carry in the store.
   * @return the modified furniture.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("carryToStore/{id}")
  @Admin
  public FurnitureDTO carryToStore(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).entity("Error: no furniture with id " + id + " found")
              .type(MediaType.TEXT_PLAIN).build());
    }
    FurnitureDTO f = furnitureUCC.carryToStore(id);
    f = Json.filterPublicJsonView(f, FurnitureDTO.class);
    return f;
  }

  /**
   * Route to change the state of a furniture from PROPO to PASCO.
   * 
   * @param furniture the furniture not suitable.
   * @return the modified furniture.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("notSuitable")
  @Admin
  public FurnitureDTO notSuitable(FurnitureDTO furniture) {
    if (furniture.getId() <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no furniture with id " + furniture.getId() + " found")
          .type(MediaType.TEXT_PLAIN).build());
    }
    return furnitureUCC.notSuitable(furniture);
  }

  /**
   * Route which allows to get furnitures in state "ENVEN" (en vente) and "ENOPT" (en option).
   * 
   * @return list of furnitures.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<FurnitureDTO> getAllFurnitures() {
    return furnitureUCC.getAllFurnitures(false);
  }

  /**
   * Route which allows to get all furnitures.
   * 
   * @return list of furnitures.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("allFurnituresAdmin")
  @Admin
  public List<FurnitureDTO> getAllFurnituresForAdmin() {
    return furnitureUCC.getAllFurnitures(true);
  }

  /**
   * Route to get an option with id_furniture and id_user.
   * 
   * @return the option wanted
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getOption/{idUser}/{idFurniture}")
  @Authorize
  public Option getAnOption(@PathParam("idUser") int idUser,
      @PathParam("idFurniture") int idFurniture) {
    if (idUser <= 0 || idFurniture <= 0) {
      throw new WebApplicationException(Response
          .status(Status.BAD_REQUEST).entity("Error: no user with id " + idUser
              + " found or no furniture with id " + idFurniture + " found")
          .type(MediaType.TEXT_PLAIN).build());
    }

    Option o = domainFactory.getOption();
    o.setFurnitureId(idFurniture);
    o.setUserID(idUser);
    return furnitureUCC.getAnOption(o);
  }

  /**
   * Route to get an option with id_furnitur.
   * 
   * @return the option wanted
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getActualOption/{idFurniture}")
  @Authorize
  public Option getActualOption(@PathParam("idFurniture") int idFurniture) {
    if (idFurniture <= 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: no furniture with id " + idFurniture + " found")
          .type(MediaType.TEXT_PLAIN).build());
    }

    Option o = domainFactory.getOption();
    o.setFurnitureId(idFurniture);
    return furnitureUCC.getAnOption(o);
  }

  /**
   * Route to create an option for a furniture which become ENOPT.
   * 
   * @param request request body built in the authorization filter with the data of the user in the
   *        property "user".
   * @param o is the option given by the form.
   * @return the new option.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("createOption")
  @Authorize
  public Option createOption(@Context ContainerRequest request, Option o) {

    if (o.getDaysLeft() == 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: You cannot have an option for this furniture anymore")
          .type(MediaType.TEXT_PLAIN).build());
    }

    UserDTO currentUser = (UserDTO) request.getProperty("user");
    int userId = currentUser.getId();

    o.setUserID(userId);

    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, o.getDuration());
    Date date = new Date(c.getTimeInMillis());
    o.setLimitDate(date);
    return furnitureUCC.createAnOption(o, o.getFurnitureId(), "ENOPT");
  }

  /**
   * Route to withdraw a furniture from sale.
   * 
   * @param id the id of the furniture.
   * @return the new state of the furniture.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("withdrawalFromSale/{id}")
  @Admin
  public FurnitureDTO withdrawalFromSale(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).entity("Error: no furniture with id " + id + " found")
              .type(MediaType.TEXT_PLAIN).build());
    }
    FurnitureDTO f = furnitureUCC.withdrawalFromSale(id);
    return f;
  }

  /**
   * Route to remove an option for a furniture which become ENVEN.
   * 
   * @param id the id of the furniture which has the option to remove.
   * @return the removed option.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("removeOption/{id}")
  @Authorize
  public Option removeOption(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).entity("Error: no furniture with id " + id + " found")
              .type(MediaType.TEXT_PLAIN).build());
    }
    Option o = domainFactory.getOption();
    o.setFurnitureId(id);
    return furnitureUCC.removeAnOption(o);
  }

  /**
   * Route which allows to get all filtred furnitures.
   * 
   * @param request request body built in the authorization filter with the data of the user in the
   *        property "user".
   * @param minPrice is the minimum price wanted.
   * @param maxPrice is the maximum price wanted.
   * @param type is the type of furniture wanted.
   * 
   * @return list of filtred furnitures.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("FiltredFurnitures/{minPrice}/{maxPrice}/{type}")
  @Authorize
  public List<FurnitureDTO> getFiltredFurnitures(@Context ContainerRequest request,
      @PathParam("minPrice") int minPrice, @PathParam("maxPrice") int maxPrice,
      @PathParam("type") int type) {
    if (maxPrice < minPrice) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: You cannot have a maximum price lower than the minimum price")
          .type(MediaType.TEXT_PLAIN).build());
    }
    if (minPrice < 0 || maxPrice < 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: You cannot have a negative price").type(MediaType.TEXT_PLAIN).build());
    }
    if (type < 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: type is not correct").type(MediaType.TEXT_PLAIN).build());
    }
    // allow to see furniture with -1 price if he is not defined.
    if (minPrice == 0 && maxPrice == 1000000000) {
      minPrice = -1;
    }
    boolean isAdmin = (boolean) request.getProperty("isAdmin");

    return furnitureUCC.getFiltredFurnitures(isAdmin, minPrice, maxPrice, type);
  }


  /**
   * Route which allows to get all filtred furnitures.
   * 
   * @param type is the type of furniture wanted.
   * 
   * @return list of filtred furnitures.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("FiltredFurnitures/{type}")
  public List<FurnitureDTO> getFiltredFurnituresQuidam(@PathParam("type") int type) {
    if (type < 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: type is not correct").type(MediaType.TEXT_PLAIN).build());
    }

    return furnitureUCC.getFiltredFurnituresQuidam(type);
  }



  /*
   * Route which allows to get all furnitures thanks to the word given.**
   * 
   * @param word is the keyword for the research.*@return a list corresponding to the keyword
   */

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("research")
  @Admin
  public List<FurnitureDTO> getFurnituressWithResearch(@QueryParam("word") String word) {
    return furnitureUCC.getFurnituresByResearch(word);
  }

  /**
   * Route which give a list of keyword available.
   * 
   * @return a list of words.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("tags")
  public List<String> getFurnituresKeywords() {
    return furnitureUCC.getTags();
  }

  /**
   * Route to add a photo to the furniture with the id in path parameter.
   * 
   * @param id the id of the furniture to add the photo.
   * @param file the photo.
   * @param fileDetails the details of the photo.
   * @return where the files have been uploaded
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("addPhoto/{id}")
  @Admin
  public Response addPhoto(@PathParam("id") int id, @FormDataParam("file") InputStream file,
      @FormDataParam("file") FormDataContentDisposition fileDetails) {
    if (file == null || fileDetails == null) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("Error: no file")
          .type(MediaType.TEXT_PLAIN).build());
    }
    if (id <= 0) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).entity("Error: no furniture with id " + id + " found")
              .type(MediaType.TEXT_PLAIN).build());
    }
    String path = Config.getProperty("imagePath") + LocalDateTime.now().toLocalDate() + "-"
        + LocalDateTime.now().getNano() + "-" + fileDetails.getFileName();
    furnitureUCC.addPhoto(id, file, path);
    return Response.ok("Photo uploaded to " + path).build();
  }

  /**
   * Route to sell a furniture (given in parameter).
   * 
   * @param f the furniture to sell.
   * @return the modified furniture(sold).
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("sellFurniture")
  @Admin
  public FurnitureDTO sellFurniture(FurnitureDTO f) {

    if (f.getId() <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no furniture with id " + f.getId() + " found").type(MediaType.TEXT_PLAIN)
          .build());
    }

    return furnitureUCC.sellFurniture(f);
  }

  /**
   * Route to change scrollable status of a furniture.
   * 
   * @param id the id of the furniture on which scrollable status is changed.
   * @param scrollable the current scrollable status of the photo.
   * @return the id of the photo and its new scrollable status.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("changeScrollable/{id}/{scrollable}")
  @Admin
  public Response changeScrollable(@PathParam("id") int id,
      @PathParam("scrollable") boolean scrollable) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no photo with id " + id + " found").type(MediaType.TEXT_PLAIN).build());
    }
    furnitureUCC.changeScrollable(id, scrollable);
    return Response.ok("Scrollable status of photo " + id + " changed to " + !scrollable).build();
  }

  /**
   * Route to change favourite photo of a furniture.
   * 
   * @param idFurniture the id of the furniture on which favourite photo is changed.
   * @param idPhoto the id of the new favourite photo.
   * @return the id of the furniture and its new favourite photo.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("changeFavouritePhoto/{idFurniture}/{idPhoto}")
  @Admin
  public Response changeFavouritePhoto(@PathParam("idFurniture") int idFurniture,
      @PathParam("idPhoto") int idPhoto) {
    if (idFurniture <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no furniture with id " + idFurniture + " found")
          .type(MediaType.TEXT_PLAIN).build());
    }
    if (idPhoto <= 0) {
      throw new WebApplicationException(
          Response.status(Status.NOT_FOUND).entity("Error: no photo with id " + idPhoto + " found")
              .type(MediaType.TEXT_PLAIN).build());
    }
    furnitureUCC.changeFavouritePhoto(idFurniture, idPhoto);
    return Response
        .ok("The favourite photo of the furniture " + idFurniture + " replaced with " + idPhoto)
        .build();
  }

}
