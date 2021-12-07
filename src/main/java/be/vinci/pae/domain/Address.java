package be.vinci.pae.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface for the type Address.
 */
@JsonDeserialize(as = AddressImpl.class)
public interface Address {

  int getId();

  void setId(int id);

  String getStreet();

  void setStreet(String street);

  String getNbr();

  void setNbr(String nbr);

  String getBox();

  void setBox(String box);

  String getPostalCode();

  void setPostalCode(String postalCode);

  String getCommune();

  void setCommune(String commune);

  String getCountry();

  void setCountry(String country);

  boolean checkIsCorrectAddress();

  boolean checkIsEmptyOrNull();
}
