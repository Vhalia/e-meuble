package be.vinci.pae.domain;

import java.sql.Date;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import be.vinci.pae.utils.VariableChecker;
import be.vinci.pae.views.Views;

/**
 * Class which represents the implementation of the interface User.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)

public class UserImpl implements User {

  @JsonView(Views.Public.class)
  private int id;

  @JsonView(Views.Public.class)
  private String userName;

  @JsonView(Views.Public.class)
  private String name;

  @JsonView(Views.Public.class)
  private String surname;

  @JsonView(Views.Public.class)
  private String email;

  @JsonView(Views.Public.class)
  private String role;

  @JsonView(Views.Public.class)
  private boolean registrationValidated;

  @JsonView(Views.Internal.class)
  private String password;

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date registrationDate;

  @JsonView(Views.Public.class)
  private int nbrFurnituresBought;

  @JsonView(Views.Public.class)
  private int nbrFurnituresSold;

  @JsonView(Views.Public.class)
  private boolean fakeClient;

  @JsonView(Views.Public.class)
  private Address address;

  /**
   * getter for the username.
   * 
   * @return the username.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * setter for the username.
   * 
   * @param userName the new username.
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * getter for the name.
   * 
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * setter for the name.
   * 
   * @param name the new name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * getter for the surname.
   * 
   * @return the surname.
   */
  public String getSurname() {
    return surname;
  }

  /**
   * setter for the surname.
   * 
   * @param surname the new surname.
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }

  /**
   * getter for the email.
   * 
   * @return the email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * setter for the email.
   * 
   * @param email the new email.
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * getter for the role.
   * 
   * @return the role.
   */
  public String getRole() {
    return role;
  }

  /**
   * setter for the role.
   * 
   * @param role the new role.
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * getter for the registrationValidated.
   * 
   * @return true if the registration of the new user is validated, false otherwise.
   */
  public boolean isRegistrationValidated() {
    return registrationValidated;
  }

  /**
   * setter for the registrationValidated.
   * 
   * @param registrationValidated boolean value which determines if the registration for the user is
   *        validated.
   */
  public void setRegistrationValidated(boolean registrationValidated) {
    this.registrationValidated = registrationValidated;
  }

  /**
   * getter for the password.
   * 
   * @return the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * setter for the password.
   * 
   * @param password the new password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * getter for the registration date.
   * 
   * @return registration date.
   */
  public Date getRegistrationDate() {
    return registrationDate;
  }

  /**
   * setter for the registration date.
   * 
   * @param registrationDate the new registration date.
   */
  public void setRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate;
  }

  /**
   * getter for the number of furnitures bought.
   * 
   * @return the number of furnitures bought.
   */
  public int getNbrFurnituresBought() {
    return nbrFurnituresBought;
  }

  /**
   * setter for the number of furnitures bought.
   * 
   * @param nbrFurnituresBought the new number of funritures bought.
   */
  public void setNbrFurnituresBought(int nbrFurnituresBought) {
    this.nbrFurnituresBought = nbrFurnituresBought;
  }

  /**
   * getter for the number of furnitures sold.
   * 
   * @return the number of furnitures sold.
   */
  public int getNbrFurnituresSold() {
    return nbrFurnituresSold;
  }

  /**
   * setter for the number of furnitures sold.
   * 
   * @param nbrFurnituresSold the new number of furnitures sold.
   */
  public void setNbrFurnituresSold(int nbrFurnituresSold) {
    this.nbrFurnituresSold = nbrFurnituresSold;
  }

  /**
   * getter to know if the user is a fake client or not.
   * 
   * @return true if the user is a fake client, false otherwise.
   */
  public boolean isFakeClient() {
    return fakeClient;
  }

  /**
   * setter for the fakeClient.
   * 
   * @param fakeClient boolean value which determines if the user is a fake client.
   */
  public void setFakeClient(boolean fakeClient) {
    this.fakeClient = fakeClient;
  }

  /**
   * getter for the address.
   * 
   * @return the address.
   */
  public Address getAddress() {
    return address;
  }

  /**
   * setter for the address.
   * 
   * @param address the new address.
   */
  public void setAddress(Address address) {
    this.address = address;
  }

  /**
   * setter for the id.
   * 
   * @param id the new id.
   */
  public void setId(int id) {
    this.id = id;
  }


  /**
   * check the password of the user.
   * 
   * @param password the plain text password to verify.
   */
  public boolean checkPassword(String password) {
    return BCrypt.checkpw(password, this.password);
  }

  /**
   * getter for the id.
   * 
   * @return the id.
   */
  @Override
  public int getId() {
    return this.id;
  }

  /**
   * crypt the password received.
   */
  @Override
  public void cryptPassword() {
    String salt = BCrypt.gensalt();
    this.password = BCrypt.hashpw(this.password, salt);
  }

  /**
   * Check if the email is valid.
   * 
   * @return true if it's valid false otherwise.
   */
  @Override
  public boolean checkEmail() {
    return Pattern.matches("^[A-Z,a-z,0-9.]+@[A-Z,a-z.]+\\.+[A-Z,a-z]+$", this.email);
  }

  /**
   * Check if the user is correct with mandatory attributes different to null and not empty.
   * 
   * @return true if the user is correct, false otherwise.
   */
  public boolean checkIsCorrectUser() {
    return VariableChecker.checkStringNotEmpty(this.userName)
        && VariableChecker.checkStringNotEmpty(this.name)
        && VariableChecker.checkStringNotEmpty(this.surname)
        && VariableChecker.checkStringNotEmpty(this.role) && this.registrationDate != null
        && checkEmail();
  }


}
