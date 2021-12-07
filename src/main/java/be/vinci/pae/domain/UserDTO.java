package be.vinci.pae.domain;

import java.sql.Date;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface for the type UserDTO which is similar to User but it contains only getters and setters.
 */
@JsonDeserialize(as = UserImpl.class)
public interface UserDTO {

  int getId();

  void setId(int id);

  String getUserName();

  void setUserName(String userName);

  String getName();

  void setName(String name);

  String getSurname();

  void setSurname(String surname);

  String getEmail();

  void setEmail(String email);

  String getRole();

  void setRole(String role);

  boolean isRegistrationValidated();

  void setRegistrationValidated(boolean registrationValidated);

  String getPassword();

  void setPassword(String password);

  Date getRegistrationDate();

  void setRegistrationDate(Date registrationDate);

  int getNbrFurnituresBought();

  void setNbrFurnituresBought(int nbrFurnituresBought);

  int getNbrFurnituresSold();

  void setNbrFurnituresSold(int nbrFurnituresSold);

  boolean isFakeClient();

  void setFakeClient(boolean fakeClient);

  Address getAddress();

  void setAddress(Address address);

}
