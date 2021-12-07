package be.vinci.pae.dataservices;

import java.util.List;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.DALErrorException;

/**
 * Interface of the type UserDAO.
 */
public interface UserDAO {

  UserDTO getUser(String username) throws DALErrorException;

  UserDTO getUser(int id) throws DALErrorException;

  boolean checkEmail(String email) throws DALErrorException;

  boolean checkUserName(String userName) throws DALErrorException;

  void createUser(UserDTO user) throws DALErrorException;

  void createAddress(Address adr) throws DALErrorException;

  void updateRole(String roleRequested, int id) throws DALErrorException;

  Address getAddress(int id) throws DALErrorException;

  List<UserDTO> getUsersNotValidated() throws DALErrorException;

  void updateUserRegistrationRefused(int id) throws DALErrorException;

  List<FurnitureDTO> getFurnituresWithAnOption(int id) throws DALErrorException;

  List<FurnitureDTO> getSoldFurnitures(int id) throws DALErrorException;

  List<FurnitureDTO> getBoughtFurnitures(int id) throws DALErrorException;

  Address getAddressFromUserId(int id) throws DALErrorException;
  
  List<UserDTO> getUsersByResearch(String word) throws DALErrorException;

  List<String> getTags() throws DALErrorException;

  void incrementNbrFurnituresSold(int userId);

  void incrementNbrFurnituresPurchased(int userId);

}
