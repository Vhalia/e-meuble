package be.vinci.pae.domain;

import java.sql.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import be.vinci.pae.utils.VariableChecker;
import be.vinci.pae.views.Views;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FurnitureImpl implements Furniture {

  @JsonView(Views.Public.class)
  private int id;

  @JsonView(Views.Public.class)
  private String description;

  @JsonView(Views.Public.class)
  private String state;

  @JsonView(Views.Public.class)
  private String type;

  @JsonView(Views.Public.class)
  private double purchasePrice;
  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date dateCarryToStore;

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date dateWithdrawalFromSale;

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date dateCarryFromClient;

  @JsonView(Views.Public.class)
  private double sellPrice;

  @JsonView(Views.Public.class)
  private double specialPrice;

  @JsonView(Views.Public.class)
  private Photo favouritePhoto;

  @JsonView(Views.Public.class)
  private List<Photo> photos;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date dateSale;

  @JsonView(Views.Public.class)
  private UserDTO purchaser;

  @JsonView(Views.Public.class)
  private UserDTO seller;

  @JsonView(Views.Public.class)
  private int visitId;

  /**
   * Allow to get the visitId.
   *
   * @return the visitId.
   */
  public int getVisitId() {
    return visitId;
  }

  /**
   * Allow to set the visitId.
   *
   * @param visitId the visitId to set.
   */
  public void setVisitId(int visitId) {
    this.visitId = visitId;
  }

  /**
   * getter for the seller.
   *
   * @return the seller.
   */
  public UserDTO getSeller() {
    return seller;
  }

  /**
   * setter for the seller.
   *
   * @param seller the new seller.
   */
  public void setSeller(UserDTO seller) {
    this.seller = seller;
  }

  /**
   * Allow to get the dateSale.
   *
   * @return the dateSale.
   */
  public Date getDateSale() {
    return dateSale;
  }

  /**
   * Allow to set the dateSale.
   *
   * @param dateSale the dateSale to set.
   */
  public void setDateSale(Date dateSale) {
    this.dateSale = dateSale;
  }

  /**
   * Allow to get the purchaser.
   *
   * @return the purchaser.
   */
  public UserDTO getPurchaser() {
    return purchaser;
  }

  /**
   * Allow to set the purchaser.
   *
   * @param purchaser the purchaser to set.
   */
  public void setPurchaser(UserDTO purchaser) {
    this.purchaser = purchaser;
  }

  /**
   * Allow to get the photos.
   *
   * @return the photos.
   */
  public List<Photo> getPhotos() {
    return photos;
  }

  /**
   * Allow to set the photos.
   *
   * @param photos the photos to set.
   */
  public void setPhotos(List<Photo> photos) {
    this.photos = photos;
  }


  /**
   * Allow to get the id.
   *
   * @return the id.
   */
  public int getId() {
    return id;
  }

  /**
   * Allow to set the id.
   *
   * @param id the id to set.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Allow to get the description.
   *
   * @return the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Allow to set the description.
   *
   * @param description the description to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Allow to get the state.
   *
   * @return the state.
   */
  public String getState() {
    return state;
  }

  /**
   * Allow to set the state.
   *
   * @param state the state to set.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * Allow to get the type.
   *
   * @return the type.
   */
  public String getType() {
    return type;
  }

  /**
   * Allow to set the type.
   *
   * @param type the type to set.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Allow to get the purchasePrice.
   *
   * @return the purchasePrice.
   */
  public double getPurchasePrice() {
    return purchasePrice;
  }

  /**
   * Allow to set the purchasePrice.
   *
   * @param purchasePrice the purchasePrice to set.
   */
  public void setPurchasePrice(double purchasePrice) {
    this.purchasePrice = purchasePrice;
  }

  /**
   * Allow to get the dateCarryToStore.
   *
   * @return the dateCarryToStore.
   */
  public Date getDateCarryToStore() {
    return dateCarryToStore;
  }

  /**
   * Allow to set the dateCarryToStore.
   *
   * @param dateCarryToStore the dateCarryToStore to set.
   */
  public void setDateCarryToStore(Date dateCarryToStore) {
    this.dateCarryToStore = dateCarryToStore;
  }

  /**
   * Allow to get the dateWithdrawalFromSale.
   *
   * @return the dateWithdrawalFromSale.
   */
  public Date getDateWithdrawalFromSale() {
    return dateWithdrawalFromSale;
  }

  /**
   * Allow to set the dateWithdrawalFromSale.
   *
   * @param dateWithdrawalFromSale the dateWithdrawalFromSale to set.
   */
  public void setDateWithdrawalFromSale(Date dateWithdrawalFromSale) {
    this.dateWithdrawalFromSale = dateWithdrawalFromSale;
  }

  /**
   * Allow to get the date CarryFromClient.
   *
   * @return the dateCarryFromClient.
   */
  public Date getDateCarryFromClient() {
    return dateCarryFromClient;
  }

  /**
   * Allow to set the dateCarryFromClient.
   *
   * @param dateCarryFromClient the dateCarryFromClient to set.
   */
  public void setDateCarryFromClient(Date dateCarryFromClient) {
    this.dateCarryFromClient = dateCarryFromClient;
  }

  /**
   * Allow to get the sellPrice.
   *
   * @return the sellPrice.
   */
  public double getSellPrice() {
    return sellPrice;
  }

  /**
   * Allow to set the sellPrice.
   *
   * @param sellPrice the sellPrice to set.
   */
  public void setSellPrice(double sellPrice) {
    this.sellPrice = sellPrice;
  }

  /**
   * Allow to get the specialPrice.
   *
   * @return the specialPrice.
   */
  public double getSpecialPrice() {
    return specialPrice;
  }

  /**
   * Allow to set the specialPrice.
   *
   * @param specialPrice the specialPrice to set.
   */
  public void setSpecialPrice(double specialPrice) {
    this.specialPrice = specialPrice;
  }

  /**
   * check if the state is equals to PROPO.
   */
  public boolean checkPropo() {
    return this.state.equals(STATES[0]);
  }

  /**
   * check if the state is equals to ENRES.
   */
  public boolean checkEnRes() {
    return this.state.equals(STATES[2]);
  }

  /**
   * check if the state is equals to ENVEN.
   */
  public boolean checkEnVen() {
    return this.state.equals(STATES[4]);
  }


  /**
   * check if the state in param is corresponding to the actual state (to be the next one).
   */
  public boolean checkNextState(String nextState) {
    // if propo next state can be enres or enmag
    if (this.state.equals(STATES[0])) {
      return nextState.equals(STATES[2]) || nextState.equals(STATES[3]);
    }
    // check if the states is ENMAG before changing to ENVEN
    if (this.state.equals(STATES[3])) {
      return nextState.equals(STATES[4]);
    }
    // check if the state is ENVEN before changing to ENOPT
    // or check ENVEN before changing to VENDU
    if (this.state.equals(STATES[4])) {
      return nextState.equals(STATES[5]) || nextState.equals(STATES[7]);
    }
    // check if the state is ENOPT before changing to ENVEN
    // or check ENOPT before changing to VENDU
    if (this.state.equals(STATES[5])) {
      return nextState.equals(STATES[4]) || nextState.equals(STATES[7]);
    }

    return false;
  }

  /**
   * Allows to get the favouritePhoto.
   *
   * @return the favouritePhoto.
   */
  public Photo getFavouritePhoto() {
    return this.favouritePhoto;
  }

  /**
   * Allows to set the photo.
   *
   * @param photo the photo to set.
   */
  public void setFavouritePhoto(Photo photo) {
    this.favouritePhoto = photo;
  }

  /**
   * check if the state is equals to ENOPT.
   */
  @Override
  public boolean checkEnOpt() {
    return this.state.equals(STATES[5]);
  }

  /**
   * if special price is different of -1, client must be an ANT.
   */
  @Override
  public boolean checkCanHaveSpecialPrice() {
    if (this.getSpecialPrice() != -1) {
      return this.getPurchaser().getRole().equals("ANT");
    }
    return true;
  }

  /**
   * Check if the furniture is correct with mandatory attributes are different to null and note
   * empty.
   * 
   * @return true if the furniture is correct, false otherwise.
   */
  public boolean checkIsCorrectFurniture() {
    return VariableChecker.checkStringNotEmpty(this.description);
  }
}
