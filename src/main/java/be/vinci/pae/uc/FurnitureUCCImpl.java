package be.vinci.pae.uc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import be.vinci.pae.dataservices.DalServices;
import be.vinci.pae.dataservices.FurnitureDAO;
import be.vinci.pae.dataservices.UserDAO;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.Furniture;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.User;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.AlreadyCancelException;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.IncorrectDurationException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.exceptions.WrongStateException;
import be.vinci.pae.utils.FileUtils;
import be.vinci.pae.utils.VariableChecker;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

public class FurnitureUCCImpl implements FurnitureUCC {
  @Inject
  private FurnitureDAO furnitureDAO;

  @Inject
  private UserDAO userDAO;

  @Inject
  private DalServices dals;

  @Inject
  private UserUCC userUcc;

  @Inject
  private DomainFactory domainFactory;

  @Override
  public Furniture fixSellPrice(int idFurniture, double sellPrice, double specialPrice,
      String nextState) {
    if (!VariableChecker.checkPositive(sellPrice)) {
      throw new IllegalArgumentException("negative price");
    }

    Furniture fu = null;
    try {
      fu = (Furniture) furnitureDAO.getFurniture(idFurniture);
      if (fu == null) {
        throw new IllegalArgumentException("inexisting furniture");
      }

      if (!fu.checkNextState(nextState)) {
        throw new WrongStateException(
            "Impossible to change the state from : " + fu.getState() + " to " + nextState);
      }
      furnitureDAO.updateSellPrice(idFurniture, sellPrice, specialPrice, nextState);
      fu.setSellPrice(sellPrice);
      fu.setSpecialPrice(specialPrice);
      fu.setState(nextState);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return fu;
  }


  /**
   * fix the purchase price of a furniture determined by the id (first parameter).
   * 
   * @param idFurniture id of the furniture.
   * @param purchasePrice puchase's price.
   * @param nextState state to go.
   * 
   * @return fu the furniture changed.
   */
  public Furniture fixPurchasePrice(int idFurniture, double purchasePrice, String nextState,
      Date dateCarryFromClient) {
    if (!VariableChecker.checkPositive(purchasePrice)) {
      throw new IllegalArgumentException("negative price");
    }

    Furniture fu = null;
    boolean isSuccessful = false;

    try {
      dals.startTransaction();
      dals.up();
      fu = (Furniture) furnitureDAO.getFurniture(idFurniture);

      if (fu == null) {
        throw new NotFoundException("inexisting furniture");
      }

      if (!fu.checkPropo() || !fu.checkNextState(nextState)) {
        throw new WrongStateException(
            "Impossible to change the state from : " + fu.getState() + " to " + nextState);
      }

      furnitureDAO.updatePurchasePrice(idFurniture, purchasePrice, nextState, dateCarryFromClient);
      fu.setPurchasePrice(purchasePrice);
      fu.setDateCarryFromClient(dateCarryFromClient);
      fu.setState(nextState);
      if (nextState.equals("ENMAG")) {
        Date date = Date.valueOf(LocalDate.now());
        furnitureDAO.updateDateCarryToStore(idFurniture, date);
        fu.setDateCarryToStore(date);
      }

      if (fu.getSeller() != null) {
        userUcc.incrementNbrFurnituresSold(fu.getSeller().getId());
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
    return fu;
  }


  /**
   * get a furniture by its id.
   * 
   * @param id the id of the furniture we want to get.
   * @param userWhoRequested the user who want to get the furniture with the id in parameter.
   * @return the furniture we want to get.
   */
  @Override
  public FurnitureDTO getFurniture(int id, UserDTO userWhoRequested) {
    FurnitureDTO furniture = null;
    try {
      furniture = furnitureDAO.getFurniture(id);
      if (furniture == null) {
        throw new NotFoundException("Error: no furniture with the id " + id + " has been found");
      }

      dals.up();
      if (!furniture.getState().equals("ENVEN") && !furniture.getState().equals("ENOPT")
          && !userWhoRequested.getRole().equals("ADM")
          && furniture.getSeller().getId() != userWhoRequested.getId()
          && (furniture.getPurchaser() == null
              || furniture.getPurchaser().getId() != userWhoRequested.getId())) {
        throw new UnauthorizedException("Error: you can't access this ressource");
      }
    } catch (

    DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      dals.closeConnection();
    }
    return furniture;
  }

  /**
   * fix the state of a furniture to ENMAG and remember the date of set to dateCarryToStore.
   * 
   * @param idFurniture id of the furniture.
   * 
   * @return fu the furniture changed.
   */
  public Furniture carryToStore(int idFurniture) {
    Furniture fu = null;
    try {
      fu = (Furniture) furnitureDAO.getFurniture(idFurniture);
      if (fu == null) {
        throw new NotFoundException("inexisting furniture");
      }

      if (!fu.checkEnRes()) {
        throw new WrongStateException(
            "a furniture with the state " + fu.getState() + " can't be carried to the store");
      }
      Date date = Date.valueOf(LocalDate.now());
      furnitureDAO.updateDateCarryToStore(idFurniture, date);
      fu.setDateCarryToStore(date);
      fu.setState("ENMAG");
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return fu;
  }


  /**
   * Allows to withdraw a furniture from sale thanks to its id.
   * 
   * @param id the id of the furniture.
   * @return the furniture which has been withdrawn.
   */
  @Override
  public FurnitureDTO withdrawalFromSale(int id) {
    Furniture f;
    try {
      f = (Furniture) furnitureDAO.getFurniture(id);
      if (f == null) {
        throw new NotFoundException("No furniture with the id " + id + " has been found");
      }
      if (!f.checkEnVen()) {
        throw new WrongStateException(
            "a furniture with the state " + f.getState() + " can't be withdrawal from sale");
      }
      Date date = Date.valueOf(LocalDate.now());
      furnitureDAO.withdrawalFromSale(id, date);
      f.setState("RETIR");
      f.setSellPrice(-1);
      f.setSpecialPrice(-1);
      f.setDateWithdrawalFromSale(date);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return f;
  }


  /**
   * create an option from the furniture and change his state.
   * 
   * @param opt is the option to create.
   * @param idFurniture is the id of the furniture.
   * @param nextState is the next state of the furniture.
   * 
   * @return opt is the option created.
   */
  public Option createAnOption(Option opt, int idFurniture, String nextState) {
    Furniture fu = null;
    try {
      fu = (Furniture) furnitureDAO.getFurniture(idFurniture);
      if (fu == null) {
        throw new NotFoundException("Error: inexisting furniture");
      }
      if (opt.getDuration() > 5 || opt.getDuration() <= 0) {
        throw new IncorrectDurationException("Error: incorrect duration ");
      }

      if (!fu.checkNextState(nextState)) {
        throw new WrongStateException(
            "Impossible to change the state from : " + fu.getState() + " to " + nextState);
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    boolean isSuccessful = false;
    // if the option has already been set.
    if (opt.getIsCancel()) {
      try {
        dals.startTransaction();
        if (opt.getDuration() > opt.getDaysLeft()) {
          throw new IncorrectDurationException("Error: incorrect duration ");
        }
        furnitureDAO.updateFurnitureStatus(idFurniture, nextState);
        fu.setState(nextState);
        if (nextState.equals("ENOPT")) {
          opt.setIsCancel(false);
          furnitureDAO.updateOption(opt);
        }
        isSuccessful = true;
      } catch (DALErrorException e) {
        throw new SomethingWentWrongException(e);
      } finally {
        if (isSuccessful) {
          dals.commitTransaction();
        } else {
          dals.rollbackTransaction();
        }
      }
      return opt;

    } else { // first time an option is set up.
      try {
        dals.startTransaction();
        furnitureDAO.updateFurnitureStatus(idFurniture, nextState);
        fu.setState(nextState);
        if (nextState.equals("ENOPT")) {
          furnitureDAO.insertOption(opt);
        }
        isSuccessful = true;
      } catch (DALErrorException e) {
        throw new SomethingWentWrongException(e);
      } finally {
        if (isSuccessful) {
          dals.commitTransaction();
        } else {
          dals.rollbackTransaction();
        }
      }
      return opt;
    }
  }

  /**
   * Allows to retrieve a list of furniture.
   * 
   * @param hasAdminPermission when set to true, it will return all the furnitures otherwise, it
   *        will display only furniture with state ENVEN (en vente) or ENOPT (en option).
   */
  @Override
  public List<FurnitureDTO> getAllFurnitures(boolean hasAdminPermission) {
    List<FurnitureDTO> furnitures = null;
    try {
      furnitures = furnitureDAO.getAllFurnitures(hasAdminPermission);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return furnitures;
  }


  /**
   * remove an option from the furniture and change his state.
   * 
   * @param option is the option to remove.
   * @return opt is the option removed.
   */
  @Override
  public Option removeAnOption(Option option) {
    Furniture fu = null;
    User us = null;
    int idFurniture = option.getFurnitureId();

    boolean isSuccessful = false;
    try {
      dals.startTransaction();
      fu = (Furniture) furnitureDAO.getFurniture(idFurniture);
      option = furnitureDAO.getOption(option);
      if (option == null) {
        throw new NotFoundException(
            "Error: furniture with the id " + idFurniture + " has no active option");
      }
      us = (User) userDAO.getUser(option.getUserId());

      if (fu == null) {
        throw new NotFoundException(
            "Error: no furniture with the id " + idFurniture + " has been found");
      }

      if (us == null) {
        throw new NotFoundException(
            "Error: no user with the id " + option.getUserId() + " has been found");
      }

      int userId = us.getId();
      if (userId != option.getUserId() && !us.getRole().equals("ADM")) {
        throw new UnauthorizedException(
            "Error: You cannot remove an option when you are not the creator");
      }

      // setting the date of today.
      Calendar c = Calendar.getInstance();
      Date date = new Date(c.getTimeInMillis());

      // setting the initial date of an option.
      Calendar co = Calendar.getInstance();
      co.setTime(option.getLimitDate());
      co.add(Calendar.DAY_OF_MONTH, option.getDuration() * -1);
      Date dateo = new Date(co.getTimeInMillis());

      // taking the difference.
      long diff = date.getTime() - dateo.getTime();
      // get the value in days.
      int daysInOption = (int) Math.round(diff / (1000 * 60 * 60 * 24));

      if (daysInOption > 5 || daysInOption < 0) {
        throw new IncorrectDurationException("Error: incorrect duration ");
      }

      if (!fu.checkNextState("ENVEN")) {
        throw new WrongStateException(
            "Impossible to change the state from : " + fu.getState() + " to ENVEN");
      }

      if (option.getIsCancel()) {
        throw new AlreadyCancelException("already cancel");
      }

      int dl = option.getDaysLeft();
      dl = dl - daysInOption;
      option.setDaysLeft(dl);
      option.setIsCancel(true);

      furnitureDAO.updateFurnitureStatus(idFurniture, "ENVEN");
      fu.setState("ENVEN");
      furnitureDAO.updateRemoveOption(option.getId(), option.getDaysLeft());
      isSuccessful = true;
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      if (isSuccessful) {
        dals.commitTransaction();
      } else {
        dals.rollbackTransaction();
      }
    }
    return option;
  }

  /**
   * Allows to retrieve an option.
   * 
   * @param opt is the option wanted.
   * @return Option the option get from the db.
   */
  @Override
  public Option getAnOption(Option opt) {
    try {
      opt = furnitureDAO.getOption(opt);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return opt;
  }

  /**
   * Allows to retrieve a list of furniture filtred.
   * 
   * @param isAdmin if the user is an admin or not.
   * @param minPrice is the minimum price wanted.
   * @param maxPrice is the maximum price wanted.
   * @param type is the type of furniture wanted.
   * 
   * @return a list of filtred furniture.
   */
  @Override
  public List<FurnitureDTO> getFiltredFurnitures(boolean isAdmin, int minPrice, int maxPrice,
      int type) {
    List<FurnitureDTO> furnitures = null;
    try {
      furnitures = furnitureDAO.getFiltredFurnitures(isAdmin, minPrice, maxPrice, type);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    return furnitures;
  }

  /**
   * Allows to retrieve a list of furniture filtred.
   * 
   * @param type is the type of furniture wanted.
   * 
   * @return a list of filtred furniture.
   */
  @Override
  public List<FurnitureDTO> getFiltredFurnituresQuidam(int type) {
    List<FurnitureDTO> furnitures = null;
    try {
      furnitures = furnitureDAO.getFiltredFurnituresQuidam(type);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    return furnitures;
  }


  /**
   * Allows to retrieve all furnitures related to a keyword in parameter.
   * 
   * @param word is the research's keyword
   * @return resultResearch is a list with all the furnitures found
   */
  public List<FurnitureDTO> getFurnituresByResearch(String word) {

    if (word == null) {
      throw new IllegalArgumentException("le mot ne peut être vide | mot clé = " + word);
    }

    List<FurnitureDTO> resultResearch = null;

    try {
      resultResearch = this.furnitureDAO.getfurnituresByResearch(word);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return resultResearch;
  }


  @Override
  public List<String> getTags() {
    List<String> tags = null;

    try {
      tags = furnitureDAO.getTags();
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    return tags;
  }

  /**
   * Allows to add a photo to a furniture with the id in parameter.
   * 
   * @param idFurniture the id of the furniture.
   * @param file the photo.
   * @param path path of the photo to add.
   * 
   * @return photo the photo added.
   */
  @Override
  public Photo addPhoto(int idFurniture, InputStream file, String path) {
    dals.startTransaction();
    boolean isSuccessful = false;
    Photo p = null;
    try {
      FurnitureDTO f = furnitureDAO.getFurniture(idFurniture);
      if (f == null) {
        throw new NotFoundException(
            "Error: no furniture with the id " + idFurniture + " has been found");
      }
      if (f.getState().equals("RETIR") || f.getState().equals("PASCO")
          || f.getState().equals("EMPOR") || f.getState().equals("LIVRE")) {
        throw new WrongStateException(
            "Error: impossible to add a photo for a furniture with the state " + f.getState());
      }
      p = domainFactory.getPhoto();
      p.setPath(path);
      p = furnitureDAO.insertPhoto(p, idFurniture);
      FileUtils.createFile(file, path);
      isSuccessful = true;
    } catch (IOException | DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      if (isSuccessful) {
        dals.commitTransaction();
      } else {
        dals.rollbackTransaction();
      }
    }
    return p;
  }


  /**
   * Allows to create a furniture. It will also create all the photos in the list of photos of the
   * furniture and it will set the favorite photo as the first one in the list.
   * 
   * @param furniture the furniture to create.
   * @param visit the visit which is link to the furniture to create.
   * @return furniture the created furniture.
   */
  @Override
  public FurnitureDTO createFurniture(FurnitureDTO furniture, VisitDTO visit,
      List<InputStream> files) {
    if (visit == null || furniture == null || furniture.getType() == null
        || furniture.getDescription() == null) {
      throw new IllegalArgumentException("Error: missing mandatory information in parameter");
    }
    if (files.size() != furniture.getPhotos().size()) {
      throw new SomethingWentWrongException("Error: incorrect number of files");
    }
    try {
      furniture.setState("PROPO");
      furniture.setSeller(visit.getClient());
      furniture = furnitureDAO.insertFurniture(furniture, visit.getId());
      for (int i = 0; i < furniture.getPhotos().size(); i++) {
        Photo p = addPhoto(furniture.getId(), files.get(i), furniture.getPhotos().get(i).getPath());
        furniture.getPhotos().get(i).setId(p.getId());
      }
      furniture = setFavoritePhoto(furniture, furniture.getPhotos().get(0).getId());
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return furniture;

  }

  /**
   * Allows to set a new favorite photo to the furniture.
   * 
   * @param furniture the furniture which its favorite photo will be changed.
   * @param idPhoto the id of the new favorite photo of the furniture.
   * @return furniture the modified furniture.
   */
  @Override
  public FurnitureDTO setFavoritePhoto(FurnitureDTO furniture, int idPhoto) {
    if (idPhoto <= 0) {
      throw new NotFoundException("Error: no photo with the id " + idPhoto + " has been found");
    }
    if (furniture.getId() <= 0) {
      throw new NotFoundException(
          "Error: no furniture with the id " + furniture.getId() + " has been found");
    }
    try {
      Photo photo = furnitureDAO.getPhoto(idPhoto);
      if (!furniture.getPhotos().contains(photo)) {
        throw new NotFoundException("Error: Impossible to set a photo as favorite if"
            + " the photo doesn't belong to the furniture");
      }
      furnitureDAO.updateFavoritePhoto(furniture, idPhoto);
      furniture.setFavouritePhoto(photo);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return furniture;
  }

  /**
   * Check if a furniture is able to be sold and set it to sold.
   * 
   * @param f the furniture to sold
   * @return the changed furniture
   */
  @Override
  public FurnitureDTO sellFurniture(FurnitureDTO f) {
    Furniture furniture = (Furniture) f;

    if (!(furniture.getPurchaser() != null && furniture.getPurchaser().getRole().equals("ANT")
        && (furniture.getState().equals("ENMAG") || furniture.getState().equals("ENRES")))) {

      if (!furniture.checkNextState("VENDU")
          || !(furniture.checkEnVen() || furniture.checkEnOpt())) {
        throw new WrongStateException(
            "Impossible to change the state from : " + furniture.getState() + " to VENDU");
      }
      if (furniture.getPurchaser() != null && !furniture.checkCanHaveSpecialPrice()) {
        throw new UnauthorizedException("Impossible to have a special price for a"
            + " no- ANT client \n actual value of special price: " + furniture.getSpecialPrice());
      }
    }
    furniture.setState("VENDU");
    furniture.setDateSale(Date.valueOf(LocalDate.now()));

    boolean isSuccessful = false;

    try {

      dals.startTransaction();
      dals.up();

      furnitureDAO.sellFurniture(furniture);
      furnitureDAO.addSale(furniture);
      furniture = (Furniture) furnitureDAO.getFurniture(furniture.getId());

      if (f.getPurchaser() != null) {
        userUcc.incrementNbrFurnituresPurchased(f.getPurchaser().getId());
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
    return furniture;
  }

  /**
   * Allows to change the scrollable status of a photo.
   * 
   * @param id the id of the photo.
   * @param scrollable the current scrollable status of the photo.
   * 
   */
  @Override
  public void changeScrollable(int id, boolean scrollable) {
    if (id <= 0) {
      throw new IllegalArgumentException("Mauvais id meuble");
    }
    try {
      furnitureDAO.changeScrollable(id, scrollable);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }

  /**
   * Allows to change the state of furnitures of a visit to "PASCO".
   * 
   * @param idVisit the id of the visit
   * 
   */
  public void cancelVisitFurniture(int idVisit) {
    if (idVisit <= 0) {
      throw new IllegalArgumentException("Error: The id of the visit is incorrect");
    }
    try {
      furnitureDAO.cancelVisitFurniture(idVisit);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }

  /**
   * Allows to get all furnitures contained in a visit.
   * 
   * @param idVisit the id of the visit which contain all the furniture that will be retrieved.
   * @return a list of all furnitures contained in the visit with the id in parameter.
   */
  @Override
  public List<FurnitureDTO> getAllFurnituresOfAVisit(int idVisit) {
    if (idVisit <= 0) {
      throw new NotFoundException("Error: no visit with the id " + idVisit);
    }

    List<FurnitureDTO> furnitures = null;
    try {
      furnitures = furnitureDAO.getAllFurnituresOfAVisit(idVisit);
      dals.up();
      for (int i = 0; i < furnitures.size(); i++) {
        UserDTO u = furnitures.get(i).getSeller();
        furnitures.get(i).setSeller(userUcc.getUser(u.getId()));
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      dals.closeConnection();
    }
    return furnitures;
  }

  /**
   * Allows to change the favourite photo of a furniture.
   * 
   * @param idFurniture the id of the furniture.
   * @param idPhoto the id of the new favourite photo.
   * 
   */
  @Override
  public void changeFavouritePhoto(int idFurniture, int idPhoto) {
    if (idFurniture <= 0) {
      throw new IllegalArgumentException("Mauvais id meuble");
    } else if (idPhoto <= 0) {
      throw new IllegalArgumentException("Mauvais id photo");
    }
    try {
      furnitureDAO.changeFavouritePhoto(idFurniture, idPhoto);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }


  /**
   * Check if a the furniture is able to be PASCO and update it.
   * 
   * @param furniture the furniture to update.
   * 
   */
  @Override
  public FurnitureDTO notSuitable(FurnitureDTO furniture) {
    if (!furniture.getState().equals("PROPO")) {
      throw new WrongStateException();
    }

    furniture.setState("PASCO");

    try {
      furnitureDAO.notSuitable(furniture);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return furniture;
  }

  /**
   * Allows to verify if an option or multiples options expired. If so, they will be remove, else
   * nothing will be done.
   * 
   * @return the number of options removed.
   */
  @Override
  public int verifyAllOptions() {
    List<Option> optionsRemoved = new ArrayList<Option>();
    try {
      List<Option> options = furnitureDAO.getAllOptionsNotCanceled();
      dals.up();
      Date now = Date.valueOf(LocalDate.now());
      for (int i = 0; i < options.size(); i++) {
        if (options.get(i).getLimitDate().before(now)) {
          Option optionRemoved = removeAnOption(options.get(i));
          optionsRemoved.add(optionRemoved);
        }
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.down();
      if (optionsRemoved.size() > 0) {
        for (int i = 0; i < optionsRemoved.size(); i++) {
          if (!optionsRemoved.get(i).getIsCancel()) {
            dals.rollbackTransaction();
            break;
          }
        }
        dals.commitTransaction();
      } else {
        dals.closeConnection();
      }

    }
    return optionsRemoved.size();
  }

  /**
   * Add a new type of furniture.
   * 
   * @param type a string which is the new libelle.
   * @return the id of the type.
   */
  @Override
  public int addType(String type) {
    return furnitureDAO.addType(type);
  }
}
