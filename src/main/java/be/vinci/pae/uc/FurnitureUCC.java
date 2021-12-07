package be.vinci.pae.uc;

import java.io.InputStream;
import java.sql.Date;
import java.util.List;
import be.vinci.pae.domain.Furniture;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;

public interface FurnitureUCC {

  FurnitureDTO getFurniture(int id, UserDTO userWhoRequested);

  Furniture fixPurchasePrice(int idFurniture, double purchasePrice, String nextState,
      Date dateCarryFromClient);

  Furniture fixSellPrice(int idFurniture, double sellPrice, double specialPrice, String nextState);

  Furniture carryToStore(int idFurniture);

  Option createAnOption(Option opt, int idFurniture, String nextState);

  FurnitureDTO withdrawalFromSale(int id);

  Option removeAnOption(Option option);

  List<FurnitureDTO> getAllFurnitures(boolean hasAdminPermission);

  List<FurnitureDTO> getFiltredFurnitures(boolean isAdmin, int minPrice, int maxPrice, int type);

  List<FurnitureDTO> getFiltredFurnituresQuidam(int type);

  Option getAnOption(Option opt);

  Photo addPhoto(int id, InputStream file, String path);

  FurnitureDTO createFurniture(FurnitureDTO furniture, VisitDTO visit, List<InputStream> files);

  FurnitureDTO setFavoritePhoto(FurnitureDTO furniture, int idPhoto);

  List<FurnitureDTO> getFurnituresByResearch(String word);

  List<String> getTags();

  FurnitureDTO sellFurniture(FurnitureDTO f);

  List<FurnitureDTO> getAllFurnituresOfAVisit(int idVisit);

  void changeScrollable(int id, boolean scrollable);

  void changeFavouritePhoto(int idFurniture, int idPhoto);

  void cancelVisitFurniture(int idVisit);

  FurnitureDTO notSuitable(FurnitureDTO furniture);

  int verifyAllOptions();

  int addType(String type);
}
