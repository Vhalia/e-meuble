package be.vinci.pae.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import be.vinci.pae.utils.VariableChecker;
import be.vinci.pae.views.Views;

/**
 * Class which represents the implementation of the Address interface.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressImpl implements Address {

  @JsonView(Views.Public.class)
  private int id;

  @JsonView(Views.Public.class)
  private String street;

  @JsonView(Views.Public.class)
  private String nbr;

  @JsonView(Views.Public.class)
  private String box;

  @JsonView(Views.Public.class)
  private String postalCode;

  @JsonView(Views.Public.class)
  private String commune;

  @JsonView(Views.Public.class)
  private String country;

  /**
   * getter for the id.
   *
   * @return the id.
   */
  public int getId() {
    return id;
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
   * getter for the street.
   *
   * @return the street.
   */
  public String getStreet() {
    return street;
  }

  /**
   * setter for the street.
   *
   * @param street the new street.
   */
  public void setStreet(String street) {
    this.street = street;
  }

  /**
   * getter for the nbr.
   *
   * @return the nbr.
   */
  public String getNbr() {
    return nbr;
  }

  /**
   * setter for the nbr.
   *
   * @param nbr the new nbr.
   */
  public void setNbr(String nbr) {
    this.nbr = nbr;
  }

  /**
   * getter for the box.
   *
   * @return the box.
   */
  public String getBox() {
    return box;
  }

  /**
   * setter for the box.
   *
   * @param box the new box.
   */
  public void setBox(String box) {
    this.box = box;
  }

  /**
   * getter for the postalCode.
   *
   * @return the postalCode.
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * setter for the postalCode.
   *
   * @param postalCode the new postalCode.
   */
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  /**
   * getter for the commune.
   *
   * @return the commune.
   */
  public String getCommune() {
    return commune;
  }

  /**
   * setter for the commune.
   *
   * @param commune the new commune.
   */
  public void setCommune(String commune) {
    this.commune = commune;
  }

  /**
   * getter for the country.
   *
   * @return the country.
   */
  public String getCountry() {
    return country;
  }

  /**
   * setter for the country.
   *
   * @param country the new country.
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * Check if the address is correct with mandatory attributes are different to null and note empty.
   * 
   * @return true if the address is correct, false otherwise.
   */
  public boolean checkIsCorrectAddress() {
    return VariableChecker.checkStringNotEmpty(this.commune)
        && VariableChecker.checkStringNotEmpty(this.street)
        && VariableChecker.checkStringNotEmpty(this.postalCode)
        && VariableChecker.checkStringNotEmpty(this.country)
        && VariableChecker.checkStringNotEmpty(this.nbr) && Integer.parseInt(this.nbr) >= 0;
  }

  /**
   * Check if all attributes of the address are null or empty.
   * 
   * @return true if all the attributes of the address are null or empty, false otherwise.
   */
  public boolean checkIsEmptyOrNull() {
    return !VariableChecker.checkStringNotEmpty(this.commune)
        && !VariableChecker.checkStringNotEmpty(this.street)
        && !VariableChecker.checkStringNotEmpty(this.postalCode)
        && !VariableChecker.checkStringNotEmpty(this.country)
        && !VariableChecker.checkStringNotEmpty(this.nbr);
  }



}
