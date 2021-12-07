package be.vinci.pae.domain;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import be.vinci.pae.views.Views;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VisitImpl implements Visit {

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date requestDate;

  @JsonView(Views.Public.class)
  private String usersTimeSlot;

  @JsonView(Views.Public.class)
  private Address storageAddress;

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private LocalDateTime visitDateTime;

  @JsonView(Views.Internal.class)
  static String[] states = {"DEM", "CONF", "ANN"};

  @JsonView(Views.Public.class)
  private String state;

  @JsonView(Views.Public.class)
  private UserDTO client;

  @JsonView(Views.Public.class)
  private String cancellationNote;

  @JsonView(Views.Public.class)
  private List<FurnitureDTO> furnitures;

  @JsonView(Views.Public.class)
  private int id;

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
   * getter for the furnitures.
   *
   * @return the furnitures.
   */
  public List<FurnitureDTO> getFurnitures() {
    return furnitures;
  }

  /**
   * setter for the furnitures.
   *
   * @param furnitures the new furnitures.
   */
  public void setFurnitures(List<FurnitureDTO> furnitures) {
    this.furnitures = furnitures;
  }

  /**
   * getter for the requestDate.
   *
   * @return the requestDate.
   */
  public Date getRequestDate() {
    return requestDate;
  }

  /**
   * setter for the requestDate.
   *
   * @param requestDate the new requestDate.
   */
  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  /**
   * getter for the usersTimeSlot.
   *
   * @return the usersTimeSlot.
   */
  public String getUsersTimeSlot() {
    return usersTimeSlot;
  }

  /**
   * setter for the usersTimeSlot.
   *
   * @param usersTimeSlot the new usersTimeSlot.
   */
  public void setUsersTimeSlot(String usersTimeSlot) {
    this.usersTimeSlot = usersTimeSlot;
  }

  /**
   * getter for the storageAddress.
   *
   * @return the storageAddress.
   */
  public Address getStorageAddress() {
    return storageAddress;
  }

  /**
   * setter for the storageAddress.
   *
   * @param storageAddress the new storageAddress.
   */
  public void setStorageAddress(Address storageAddress) {
    this.storageAddress = storageAddress;
  }

  /**
   * getter for the visitDateTime.
   *
   * @return the visitDateTime.
   */
  public LocalDateTime getVisitDateTime() {
    return visitDateTime;
  }

  /**
   * setter for the visitDateTime.
   *
   * @param visitDateTime the new visitDateTime.
   */
  public void setVisitDateTime(LocalDateTime visitDateTime) {
    this.visitDateTime = visitDateTime;
  }

  /**
   * getter for the state.
   *
   * @return the state.
   */
  public String getState() {
    return state;
  }

  /**
   * setter for the state.
   *
   * @param state the new state.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * getter for the client.
   *
   * @return the client.
   */
  public UserDTO getClient() {
    return client;
  }

  /**
   * setter for the client.
   *
   * @param client the new client.
   */
  public void setClient(UserDTO client) {
    this.client = client;
  }

  /**
   * getter for the cancellationNote.
   *
   * @return the cancellationNote.
   */
  public String getCancellationNote() {
    return cancellationNote;
  }

  /**
   * setter for the cancellationNote.
   *
   * @param cancellationNote the new cancellationNote.
   */
  public void setCancellationNote(String cancellationNote) {
    this.cancellationNote = cancellationNote;
  }

  /**
   * Check if the visit is DEM.
   * 
   * @return true if it is
   */
  @Override
  public boolean checkDEM() {
    return this.state.equals(states[0]);
  }


}
