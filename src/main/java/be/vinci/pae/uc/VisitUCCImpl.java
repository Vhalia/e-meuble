package be.vinci.pae.uc;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import be.vinci.pae.dataservices.DalServices;
import be.vinci.pae.dataservices.VisitDAO;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.Furniture;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.User;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.Visit;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.WrongStateException;
import be.vinci.pae.utils.VariableChecker;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

public class VisitUCCImpl implements VisitUCC {

  @Inject
  VisitDAO visitDAO;

  @Inject
  FurnitureUCC furnitureUCC;

  @Inject
  UserUCC userUCC;

  @Inject
  DalServices dals;

  /**
   * Allows to create a visit request. It will create all the furniture and its photos contained in
   * the visit.
   * 
   * @param visit the visit to be created.
   * @param files list of photos of furniture contained in the visit request.
   * @param createFakeClient boolean which determine if the methods needs to create a fake client
   *        with the information of the client in the visit request.
   * @return the visit created.
   */
  @Override
  public VisitDTO createVisitRequest(VisitDTO visit, List<InputStream> files,
      boolean createFakeClient) {
    visit.setState("DEM");
    visit.setRequestDate(Date.valueOf(LocalDate.now()));

    if (visit.getCancellationNote() != null) {
      throw new IllegalArgumentException("Error: There should not be a cancellation note");
    }
    if (visit.getClient() == null) {
      throw new IllegalArgumentException("Error: No client set as requester of the visit");
    }

    boolean isSuccessful = false;
    try {
      dals.startTransaction();
      dals.up();

      if (createFakeClient) {
        String pseudo = visit.getClient().getName() + "-" + visit.getClient().getSurname();
        UserDTO fakeClient = userUCC.createFakeClient(visit.getClient().getName(),
            visit.getClient().getSurname(), pseudo, visit.getClient().getEmail());
        visit.setClient(fakeClient);
      }

      User userToTest = (User) visit.getClient();

      if (!userToTest.checkIsCorrectUser()) {
        throw new IllegalArgumentException(
            "Error: Mandatory information related to the client is incorrect");
      }

      if (visit.getStorageAddress() == null || visit.getStorageAddress().checkIsEmptyOrNull()) {
        visit.setStorageAddress(userUCC.getAddress(visit.getClient()));
      } else {
        userUCC.addAddress(visit.getStorageAddress());
      }

      if (!VariableChecker.checkStringNotEmpty(visit.getUsersTimeSlot())
          || visit.getFurnitures() == null || visit.getFurnitures().isEmpty()) {
        throw new IllegalArgumentException("Error: missing mandatory information");
      }

      visit = visitDAO.insertVisitRequest(visit);
      int indexFile = 0;
      List<FurnitureDTO> furnitures = visit.getFurnitures();
      for (int i = 0; i < furnitures.size(); i++) {
        Furniture f = (Furniture) furnitures.get(i);
        if (!f.checkIsCorrectFurniture() || f.getPhotos() == null || f.getPhotos().isEmpty()) {
          throw new IllegalArgumentException(
              "Error: missing mandatory information concerning the furniture");
        }
        // Stream that allows to retrieve from the list of all files send in the visit request only
        // the files
        // concerning a furniture.
        List<InputStream> filesOfAFurniture =
            files.stream().skip(indexFile).limit(f.getPhotos().size()).collect(Collectors.toList());
        furnitureUCC.createFurniture(f, visit, filesOfAFurniture);
        indexFile = f.getPhotos().size();
      }
      isSuccessful = true;
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      if (isSuccessful) {
        dals.commitTransaction();
      } else {
        dals.rollbackTransaction();
      }
    }

    return visit;
  }

  /**
   * Allows to get all the visits.
   * 
   * @return the list of all visits.
   */
  public List<VisitDTO> getAllVisits() {
    List<VisitDTO> visits = null;
    try {
      visits = visitDAO.getAllVisits();
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return visits;
  }

  /**
   * Allows to get a visit according to its id in parameter.
   * 
   * @param idVisit the id of the visit to retrieve.
   * @return the visit with the id in parameter.
   */
  @Override
  public VisitDTO getVisit(int idVisit) {
    VisitDTO visit = null;
    try {
      visit = visitDAO.getVisit(idVisit);
      dals.up();
      if (visit == null) {
        throw new NotFoundException("Error: no visit with the id " + idVisit + " has been found");
      }
      UserDTO client = userUCC.getUser(visit.getClient().getId());
      visit.setClient(client);
      Address storageAddress = userUCC.getAddress(visit.getStorageAddress().getId());
      visit.setStorageAddress(storageAddress);
      List<FurnitureDTO> furnitures = furnitureUCC.getAllFurnituresOfAVisit(idVisit);
      visit.setFurnitures(furnitures);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      dals.closeConnection();
    }
    return visit;
  }

  /**
   * allow to change the visit to cancelled.
   * 
   * @param visit is the visit to cancel.
   * 
   * @return the cancelled visit.
   */
  @Override
  public VisitDTO cancelAVisit(VisitDTO visit) {
    if (!visit.getState().equals("DEM")) {
      throw new WrongStateException(
          "Impossible to cancel the visit with the state : " + visit.getState());
    }
    boolean isSuccessful = false;
    try {
      dals.startTransaction();
      dals.up();
      visit = this.visitDAO.cancelAVisit(visit);
      furnitureUCC.cancelVisitFurniture(visit.getId());
      isSuccessful = true;
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      if (isSuccessful) {
        dals.commitTransaction();
      } else {
        dals.rollbackTransaction();
      }
    }
    return visit;
  }

  /*
   * Check the attributes of a visit. If the visit is conform, change the state to CONF.
   * 
   * @param visit the visit to confirm
   * 
   * @return the confirmed visit
   */
  @Override
  public VisitDTO confirmVisit(VisitDTO visit) {

    Visit v = (Visit) visit;

    if (!v.checkDEM()) {
      throw new WrongStateException("Visit must be at state DEM but is " + visit.getState());
    }

    if (visit.getVisitDateTime() == null) {
      throw new IllegalArgumentException("Visit must have a visitDateTime to be confirmed");
    }

    visit.setState("CONF");

    try {
      visitDAO.confirmVisit(visit);

    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    return visit;
  }

  /**
   * Allows to get the visits of a user.
   * 
   * @param id the id of the user.
   * @return a list of furniture
   */
  @Override
  public List<VisitDTO> getVisitsOfAUser(int id) {
    List<VisitDTO> visits = null;
    try {
      visits = visitDAO.getVisitsOfAUser(id);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return visits;
  }

}
