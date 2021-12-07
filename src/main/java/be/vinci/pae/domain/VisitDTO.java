package be.vinci.pae.domain;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = VisitImpl.class)
public interface VisitDTO {

  Date getRequestDate();

  void setRequestDate(Date requestDate);

  String getUsersTimeSlot();

  void setUsersTimeSlot(String usersTimeSlot);

  Address getStorageAddress();

  void setStorageAddress(Address storageAddress);

  LocalDateTime getVisitDateTime();

  void setVisitDateTime(LocalDateTime visitDateTime);

  String getState();

  void setState(String state);

  UserDTO getClient();

  void setClient(UserDTO client);

  String getCancellationNote();

  void setCancellationNote(String cancellationNote);

  List<FurnitureDTO> getFurnitures();

  void setFurnitures(List<FurnitureDTO> furnitures);

  int getId();

  void setId(int id);
}
