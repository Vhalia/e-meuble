package be.vinci.pae.domain;

import java.sql.Date;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FurnitureImpl.class)
public interface FurnitureDTO {
  int getId();

  void setId(int id);

  String getDescription();

  void setDescription(String description);

  String getState();

  void setState(String state);

  double getPurchasePrice();

  void setPurchasePrice(double purchasePrice);

  Date getDateCarryToStore();

  void setDateCarryToStore(Date dateCarryToStore);

  Date getDateWithdrawalFromSale();

  void setDateWithdrawalFromSale(Date dateRemoveFromSale);

  Date getDateCarryFromClient();

  void setDateCarryFromClient(Date dateCarryToStore);

  double getSellPrice();

  void setSellPrice(double sellPrice);

  double getSpecialPrice();

  void setSpecialPrice(double specialPrice);

  String getType();

  void setType(String type);

  Photo getFavouritePhoto();

  void setFavouritePhoto(Photo photo);

  List<Photo> getPhotos();

  void setPhotos(List<Photo> photos);

  Date getDateSale();

  void setDateSale(Date dateSale);

  UserDTO getPurchaser();

  void setPurchaser(UserDTO purchaser);

  UserDTO getSeller();

  void setSeller(UserDTO user);

  int getVisitId();

  void setVisitId(int visitId);
}
