
package be.vinci.pae.uc;

import java.util.List;
import be.vinci.pae.dataservices.DalServices;
import be.vinci.pae.dataservices.UserDAO;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.User;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.utils.VariableChecker;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

public class UserUCCImpl implements UserUCC {

  @Inject
  private UserDAO userDAO;

  @Inject
  private DalServices dals;

  @Inject
  private DomainFactory domaineFactory;

  /**
   * Allows to connect an user.
   * 
   * @param username the username of the user who is trying to connect.
   * @param password the password of the user.
   * @return user the user connected.
   */
  @Override
  public User login(String username, String password) {
    User user = null;
    try {
      user = (User) this.userDAO.getUser(username);
      if (user == null || !user.checkPassword(password)) {
        throw new UnauthorizedException("Error: Wrong password or username");
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return user;
  }

  /**
   * Allows to get an user from its id.
   * 
   * @param id the id of the user.
   * @return user the user with the id.
   */
  @Override
  public UserDTO getUser(int id) {
    UserDTO user = null;
    try {
      user = this.userDAO.getUser(id);

      if (user == null) {
        throw new NotFoundException("Error: no user with the id: " + id + " has been found");
      }

    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return user;
  }

  /**
   * Allows to get the user associated with the userName in parameter.
   * 
   * @param userName the username of the user to retrieve.
   * @return the user with the username in parameter.
   */
  public UserDTO getUser(String userName) {
    if (!VariableChecker.checkStringNotEmpty(userName)) {
      throw new IllegalArgumentException("Error: incorrect username");
    }
    UserDTO userRetrieved = null;
    try {
      userRetrieved = userDAO.getUser(userName);
      if (userRetrieved == null) {
        throw new NotFoundException("No user with username " + userName + " has been found");
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return userRetrieved;
  }

  /**
   * Allows to register a new user.
   * 
   * @param userDto all the data related to the new user.
   * @return user the new user.
   */
  @Override
  public User register(UserDTO userDto) {
    User user = (User) userDto;
    user.cryptPassword();
    if (!user.checkEmail()) {
      throw new UnauthorizedException("Error: Wrong email format !");
    }
    if (!User.checkRole(userDto.getRole())) {
      throw new UnauthorizedException("Error: Wrong role !");
    }

    dals.startTransaction();
    boolean isSuccessful = false;
    try {
      if (this.userDAO.checkEmail(userDto.getEmail())
          && this.userDAO.checkUserName(userDto.getUserName())) {
        this.userDAO.createAddress(user.getAddress());
        this.userDAO.createUser(user);
        isSuccessful = true;
      } else {
        throw new UnauthorizedException("Error: Email or Username already used !");
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      if (isSuccessful) {
        dals.commitTransaction();
      } else {
        dals.rollbackTransaction();
      }
    }
    return user;
  }

  /**
   * Allows to validate the registration of an user with the role that he requested.
   * 
   * @param u the user that will eventually be validate.
   * @param roleRequested the role requested by the user.
   */
  @Override
  public UserDTO validateRegister(UserDTO u, String roleRequested) {
    if (!User.checkRole(roleRequested)) {
      throw new UnauthorizedException("Error: the role requested is not correct");
    }
    UserDTO userDb = null;
    try {
      userDb = this.userDAO.getUser(u.getUserName());
      if (userDb == null) {
        throw new NotFoundException(
            "Error: no user with username " + u.getUserName() + " has been found");
      }
      if (!userDb.isRegistrationValidated()) {
        this.userDAO.updateRole(u.getRole(), userDb.getId());
      } else {
        throw new UnauthorizedException(
            "Error: the registration of this user has already been validated");
      }
      userDb.setRegistrationValidated(true);
      userDb.setRole(roleRequested);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return userDb;
  }

  /**
   * Allows to get a list of user not validated.
   * 
   * @return list of user not validated.
   */
  @Override
  public List<UserDTO> getUsersNotValidated() {
    try {
      List<UserDTO> users = userDAO.getUsersNotValidated();
      return users;
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }

  /**
   * Allows to refuse the registration of an user.
   * 
   * @param user the user who's registration will be refuse.
   */
  @Override
  public UserDTO refuseRegister(UserDTO user) {
    UserDTO userFound = null;
    try {
      userFound = userDAO.getUser(user.getUserName());
      if (userFound == null) {
        throw new NotFoundException(
            "Error: no user with username " + user.getUserName() + " has been found");
      }
      if (!userFound.isRegistrationValidated()) {
        this.userDAO.updateUserRegistrationRefused(userFound.getId());
      } else {
        throw new UnauthorizedException(
            "Error: the registration of this user has already been validated");
      }
      userFound.setRole("CLI");
      userFound.setRegistrationValidated(true);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return userFound;
  }

  /**
   * Allows to retrieve all furnitures with an option set by an user with the id in parameter.
   * 
   * @param id the id of the user who set the option.
   * @return furnituresOption a list of furniture in state 'ENOPT' (en option).
   */
  @Override
  public List<FurnitureDTO> getFurnituresWithAnOption(int id) {
    List<FurnitureDTO> furnituresOption = null;
    try {
      furnituresOption = this.userDAO.getFurnituresWithAnOption(id);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return furnituresOption;
  }

  /**
   * Allows to get the address of the user in parameter.
   * 
   * @param user the user.
   * @return the address of the user.
   */
  @Override
  public Address getAddress(UserDTO user) {
    Address adr = null;
    try {
      adr = this.userDAO.getAddressFromUserId(user.getId());
      if (adr == null) {
        throw new NotFoundException(
            "Error: no address for the user with the id " + user.getId() + " has been found");
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return adr;
  }

  /**
   * Allows to get the address of thanks to its id in parameter.
   * 
   * @param idAddress the id of the address to retrieve.
   * @return the address with the id in parameter.
   */
  @Override
  public Address getAddress(int idAddress) {
    Address adr = null;
    try {
      adr = this.userDAO.getAddress(idAddress);
      if (adr == null) {
        throw new NotFoundException(
            "Error: no address for the user with the id " + idAddress + " has been found");
      }
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return adr;
  }
  
  /**  
   * Allows to retrieve all furnitures bought by an user with the id in parameter.
   * 
   * @param id the id of the user who sold the furnitures.
   * @return boughtFurnitures a list of furnitures bought by a user.
   */
  public List<FurnitureDTO> getBoughtFurnitures(int id) {
    List<FurnitureDTO> boughtFurnitures = null;
    try {
      boughtFurnitures = this.userDAO.getBoughtFurnitures(id);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return boughtFurnitures;
  }
    
  /**
   * Allows to retrieve all furnitures sold by an user with the id in parameter.
   * 
   * @param id the id of the user who sold the furnitures.
   * @return soldFurnitures a list of furnitures sold by a user.
   */
  public List<FurnitureDTO> getSoldFurnitures(int id) {
    List<FurnitureDTO> soldFurnitures = null;
    try {
      soldFurnitures = this.userDAO.getSoldFurnitures(id);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    } 
    return soldFurnitures;
  }

  /**
   * Allows to add a new address.
   * 
   * @param address the address to add.
   */
  @Override
  public void addAddress(Address address) {
    if (address == null || !address.checkIsCorrectAddress()) {
      throw new IllegalArgumentException("Error: the address in parameter is incorrect");
    }
    try {
      this.userDAO.createAddress(address);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }

  /**
   * Allows to retrieve all users related to a keyword in parameter.
   * 
   * @param word is the research's keyword
   * @return resultResearch is a list with all the users found
   */
  public List<UserDTO> getUsersByResearch(String word) {

    if (word == null) {
      throw new IllegalArgumentException("le mot ne peut être vide | mot clé = " + word);
    }

    List<UserDTO> resultResearch = null;

    try {
      resultResearch = this.userDAO.getUsersByResearch(word);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return resultResearch;
  }

  /**
   * Return a list of tag corresponding to users in db.
   * 
   * @return tags a String's list
   */
  @Override
  public List<String> getTags() {
    List<String> tags = null;

    try {
      tags = userDAO.getTags();
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

    return tags;
  }

  /**
   * Allows to create a fake client.
   * 
   * @param name the name of the fake user.
   * @param surname the surname of the fake user.
   * @param username the username of the fake user.
   * @param email the email of the fake user.
   * @return the fake user created.
   */
  public UserDTO createFakeClient(String name, String surname, String username, String email) {
    if (!VariableChecker.checkStringNotEmpty(name) || !VariableChecker.checkStringNotEmpty(surname)
        || !VariableChecker.checkStringNotEmpty(username)
        || !VariableChecker.checkStringNotEmpty(email)) {
      throw new IllegalArgumentException(
          "Error: Missing mandatory information concerning the fake client");
    }

    UserDTO fakeUser = domaineFactory.getUser();
    try {
      fakeUser.setFakeClient(true);
      fakeUser.setName(name);
      fakeUser.setSurname(surname);
      fakeUser.setUserName(username);
      fakeUser.setEmail(email);
      fakeUser.setRole("CLI");
      fakeUser.setRegistrationValidated(true);
      userDAO.createUser(fakeUser);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
    return fakeUser;
  }


  /**
   * Call the userDAO to increment the number of sold furnitures of a user. This method is only call
   * when a furniture has been purchased by a user. (so from FurnitureUcc).
   * 
   * @param userId the id of a user.
   */
  @Override
  public void incrementNbrFurnituresSold(int userId) {
    try {
      userDAO.incrementNbrFurnituresSold(userId);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }

  }

  /**
   * Call the userDAO to increment the number of purchased furnitures of a user. This method is only
   * call when a furniture has been sold by an admin. (so from FurnitureUcc).
   * 
   * @param userId the id of a user.
   */
  @Override
  public void incrementNbrFurnituresPurchased(int userId) {
    try {
      userDAO.incrementNbrFurnituresPurchased(userId);
    } catch (DALErrorException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      dals.closeConnection();
    }
  }
}
