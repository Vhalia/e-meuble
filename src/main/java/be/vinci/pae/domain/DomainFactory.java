package be.vinci.pae.domain;

/**
 * Interface of the factory for the all domain types.
 */
public interface DomainFactory {

  UserDTO getUser();

  Address getAddress();

  Furniture getFurniture();

  Option getOption();

  Photo getPhoto();

  VisitDTO getVisit();

}
