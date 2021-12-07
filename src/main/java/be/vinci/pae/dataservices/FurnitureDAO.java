package be.vinci.pae.dataservices;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.exceptions.DALErrorException;

public interface FurnitureDAO {

  FurnitureDTO getFurniture(int idFurniture) throws DALErrorException;

  void updatePurchasePrice(int idFurniture, double purchasePrice, String nextState,
      Date dateCarryFromClient) throws DALErrorException;

  void updateDateCarryToStore(int idFurniture, Date date) throws DALErrorException;

  void withdrawalFromSale(int idFurniture, Date date) throws DALErrorException;

  List<FurnitureDTO> getAllFurnitures(boolean hasAdminPermission) throws DALErrorException;

  void updateSellPrice(int idFurniture, double sellPrice, double specialPrice, String nextState)
      throws DALErrorException;

  void insertOption(Option option) throws DALErrorException;

  void updateFurnitureStatus(int idFurniture, String nextState) throws DALErrorException;

  void updateRemoveOption(int idOption, int daysLeft) throws DALErrorException;

  void updateOption(Option opt) throws DALErrorException;

  Option getOption(Option opt) throws DALErrorException;

  List<FurnitureDTO> getfurnituresByResearch(String word) throws DALErrorException;

  List<String> getTags() throws DALErrorException;

  /**
   * set attributes of a furnitures given.
   * 
   * @param rs the resultSet.
   * @param f the furniture to set.
   * @throws SQLException exception from dataBase.
   */
  static void setAttributes(ResultSet rs, FurnitureDTO f) throws DALErrorException {
    try {
      f.setId(rs.getInt("id_meuble"));
      f.setDescription(rs.getString("description"));
      f.setState(rs.getString("m_etat"));
      f.setType(rs.getString("libelle"));
      f.setPurchasePrice(rs.getDouble("prix_achat"));
      f.setDateCarryFromClient(rs.getDate("date_emporte_mag"));
      f.setDateCarryToStore(rs.getDate("date_depot_en_mag"));
      f.setDateWithdrawalFromSale(rs.getDate("date_retrait"));
      f.setSellPrice(rs.getDouble("prix_vente"));
      f.setSpecialPrice(rs.getDouble("prix_special"));
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  Photo getPhoto(int idPhoto) throws DALErrorException;

  Photo insertPhoto(Photo photo, int idFurniture) throws DALErrorException;

  int getTypeFromLabel(String label) throws DALErrorException;

  FurnitureDTO insertFurniture(FurnitureDTO furnitureDTO, int idVisit) throws DALErrorException;

  void updateFavoritePhoto(FurnitureDTO furniture, int photoId) throws DALErrorException;

  void addSale(FurnitureDTO furniture) throws DALErrorException;

  List<FurnitureDTO> getFiltredFurnitures(boolean isAdmin, int minPrice, int maxPrice, int type)
      throws DALErrorException;

  List<FurnitureDTO> getFiltredFurnituresQuidam(int type) throws DALErrorException;

  void sellFurniture(FurnitureDTO furniture) throws DALErrorException;

  List<FurnitureDTO> getAllFurnituresOfAVisit(int idVisit) throws DALErrorException;

  void changeScrollable(int id, boolean scrollable) throws DALErrorException;

  void changeFavouritePhoto(int idFurniture, int idPhoto) throws DALErrorException;

  void cancelVisitFurniture(int idVisit) throws DALErrorException;

  void notSuitable(FurnitureDTO furniture) throws DALErrorException;

  List<Option> getAllOptionsNotCanceled() throws DALErrorException;

  int addType(String type);
}
