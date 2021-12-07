package be.vinci.pae.api;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ContainerRequest;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.filters.Admin;
import be.vinci.pae.filters.Authorize;
import be.vinci.pae.uc.FurnitureUCC;
import be.vinci.pae.uc.UserUCC;
import be.vinci.pae.uc.VisitUCC;
import be.vinci.pae.utils.Config;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Class which represents the resource for the visit.
 */
@Path("/visits")
@Singleton
public class VisitRessource {

  @Inject
  VisitUCC visitUCC;

  @Inject
  FurnitureUCC furnitureUCC;

  @Inject
  UserUCC userUCC;

  /**
   * Route which allows to create a visit request.
   * 
   * @param filesPart List of all the files of the furnitures in the visit request.
   * @param jsonPart Contain all the json related to the visit data.
   * @param createFakeClient boolean which determines if this route needs to create a fake client or
   *        not.
   * @param request contains the data of the user who requested the route.
   * @return the new visit.
   */
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Authorize
  public VisitDTO createVisitRequest(@FormDataParam("file") List<FormDataBodyPart> filesPart,
      @FormDataParam("visit") FormDataBodyPart jsonPart,
      @FormDataParam("createFakeClient") boolean createFakeClient,
      @Context ContainerRequest request) {

    if (jsonPart == null || filesPart == null) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: Missing visit data and/or photos of the furniture in the visit")
          .type(MediaType.TEXT_PLAIN).build());
    }

    jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
    VisitDTO visit = jsonPart.getValueAs(VisitDTO.class);

    UserDTO userWhoRequested = (UserDTO) request.getProperty("user");
    if (userWhoRequested.getRole().equals("ADM")) {
      if (visit.getClient() == null) {
        throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
            .entity("Error: No client has been set as requester of the visit")
            .type(MediaType.TEXT_PLAIN).build());
      }
      if (!createFakeClient) {
        visit.setClient(userUCC.getUser(visit.getClient().getUserName()));
      }
    } else {
      visit.setClient(userWhoRequested);
    }

    List<InputStream> files = new LinkedList<InputStream>();
    int fileIndex = 0;
    List<FurnitureDTO> furnitures = visit.getFurnitures();
    for (int i = 0; i < furnitures.size(); i++) {
      List<Photo> photos = furnitures.get(i).getPhotos();
      for (int j = 0; j < photos.size(); j++) {
        String path = Config.getProperty("imagePath") + LocalDateTime.now().toLocalDate() + "-"
            + LocalDateTime.now().getNano() + "-"
            + filesPart.get(fileIndex).getFormDataContentDisposition().getFileName();
        photos.get(j).setPath(path);
        files.add(filesPart.get(fileIndex).getValueAs(InputStream.class));
        fileIndex++;
      }
    }

    return visitUCC.createVisitRequest(visit, files, createFakeClient);
  }

  /**
   * Route which allows to get all the visits.
   * 
   * @return list of all visit.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Admin
  public List<VisitDTO> getAllVisits() {
    return visitUCC.getAllVisits();
  }

  /**
   * Route which allows to get a visit according to its id.
   * 
   * @param id the id of the visit to get.
   * @return the visit with the id in path parameter.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{id}")
  @Authorize
  public VisitDTO getVisit(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: incorrect id " + id).type(MediaType.TEXT_PLAIN).build());
    }
    return visitUCC.getVisit(id);
  }

  /**
   * Route to cancel a visit.
   * 
   * @param v is the visit to cancel.
   * 
   * @return the cancelled option.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("cancelVisit")
  @Admin
  public VisitDTO cancelVisit(VisitDTO v) {
    if (v.getId() <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no visit with id " + v.getId() + " found").type(MediaType.TEXT_PLAIN)
          .build());
    }
    if (v.getCancellationNote() == null) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: you need to entre a justification").type(MediaType.TEXT_PLAIN).build());
    }
    v = this.visitUCC.cancelAVisit(v);
    return v;
  }

  /**
   * Route to confirm a visit Request.
   * 
   * @param visit The visit to confirm
   * 
   * @return visit the visit confirmed
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("confirm")
  @Admin
  public VisitDTO confirmVisit(VisitDTO visit) {

    if (visit == null) {
      throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
          .entity("Error: Missing visit data").type(MediaType.TEXT_PLAIN).build());
    }

    if (visit.getId() <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no visit with id " + visit.getId() + " found").type(MediaType.TEXT_PLAIN)
          .build());
    }

    return visitUCC.confirmVisit(visit);
  }


  /**
   * Route which allows to get all the visits of a specific user.
   * 
   * @param id of the user.
   * @return list of all visit of the user.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getVisits/{id}")
  @Authorize
  public List<VisitDTO> getVisitsOfAUser(@PathParam("id") int id) {
    if (id <= 0) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND)
          .entity("Error: no user with id " + id + " found").type(MediaType.TEXT_PLAIN).build());
    }
    return visitUCC.getVisitsOfAUser(id);
  }
}
