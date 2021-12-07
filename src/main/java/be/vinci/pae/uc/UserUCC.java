package be.vinci.pae.uc;

import java.util.List;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.User;
import be.vinci.pae.domain.UserDTO;

public interface UserUCC {

  UserDTO login(String username, String password);

  UserDTO getUser(int id);

  UserDTO getUser(String userName);

  User register(UserDTO user);

  UserDTO validateRegister(UserDTO u, String roleRequested);

  List<UserDTO> getUsersNotValidated();

  UserDTO refuseRegister(UserDTO u);

  List<FurnitureDTO> getFurnituresWithAnOption(int id);

  List<FurnitureDTO> getSoldFurnitures(int id);

  List<FurnitureDTO> getBoughtFurnitures(int id);
  
  Address getAddress(UserDTO user);

  Address getAddress(int idAddress);

  void addAddress(Address address);

  List<UserDTO> getUsersByResearch(String word);

  List<String> getTags();

  void incrementNbrFurnituresPurchased(int userId);

  void incrementNbrFurnituresSold(int userId);

  UserDTO createFakeClient(String name, String surname, String username, String email);

}
