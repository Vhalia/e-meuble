package be.vinci.pae.domain;

/**
 * Class which represents the implementation of the domainFactory interface.
 */
public class DomainFactoryImpl implements DomainFactory {

  /**
   * Allows to give an implementation of the type User.
   */
  @Override
  public UserDTO getUser() {
    return new UserImpl();
  }

  /**
   * Allows to give an implementation of the type Address.
   */
  @Override
  public Address getAddress() {
    return new AddressImpl();
  }


  /**
   * Allows to give an implementation of the type Furniture.
   */
  @Override
  public Furniture getFurniture() {
    return new FurnitureImpl();
  }

  /**
   * Allows to give an implementation of the type Option.
   */
  @Override
  public Option getOption() {
    return new OptionImpl();
  }

  /**
   * Allows to give an implementation of the type Photo.
   */
  @Override
  public Photo getPhoto() {
    return new PhotoImpl();
  }

  /**
   * Allows to give an implementation of the type Photo.
   */
  @Override
  public VisitDTO getVisit() {
    return new VisitImpl();
  }

}
