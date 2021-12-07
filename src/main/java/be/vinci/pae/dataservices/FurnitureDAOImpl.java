package be.vinci.pae.dataservices;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.utils.FileUtils;
import jakarta.inject.Inject;

public class FurnitureDAOImpl implements FurnitureDAO {

  @Inject
  private DomainFactory domainFactory;

  @Inject
  private DalBackendServices dals;

  /**
   * get a furniture from the database with the id.
   * 
   * @param idFurniture the id of the furniture wanted.
   * @return the modified furniture.
   */
  public FurnitureDTO getFurniture(int idFurniture) throws DALErrorException {
    FurnitureDTO furniture = null;

    try (Statement s = dals.getStatement()) {
      ResultSet rs = s.executeQuery("SELECT t.libelle, m.*, v.id_client, v.date_vente "
          + "FROM projet.meubles m JOIN projet.types t ON m.type = t.id_type "
          + "LEFT OUTER JOIN projet.ventes v ON m.id_meuble = v.id_meuble " + "WHERE m.id_meuble = "
          + idFurniture);



      if (rs.next()) {
        furniture = this.domainFactory.getFurniture();
        furniture.setType(rs.getString("libelle"));
        furniture.setDescription(rs.getString("description"));
        furniture.setState(rs.getString("m_etat"));
        furniture.setPurchasePrice(rs.getDouble("prix_achat"));
        furniture.setSellPrice(rs.getDouble("prix_vente"));
        furniture.setSpecialPrice(rs.getDouble("prix_special"));
        furniture.setDateCarryFromClient(rs.getDate("date_emporte_mag"));
        furniture.setDateCarryToStore(rs.getDate("date_depot_en_mag"));
        furniture.setDateWithdrawalFromSale(rs.getDate("date_retrait"));
        furniture.setVisitId(rs.getInt("visite"));
        furniture.setId(idFurniture);
        furniture.setFavouritePhoto(getPhoto(rs.getInt("photo_preferee")));
        furniture.setPhotos(getAllPhotosOfAFurniture(furniture.getId()));

        // seller
        furniture.setSeller(this.domainFactory.getUser());
        furniture.getSeller().setId(rs.getInt("client"));

        // purchaser
        if (rs.getInt("id_client") != 0) {
          furniture.setDateSale(rs.getDate("date_vente"));
          furniture.setPurchaser(this.domainFactory.getUser());
          furniture.getPurchaser().setId(rs.getInt("id_client"));
        }
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return furniture;
  }


  /**
   * update the state and the purchase price of a furniture.
   * 
   * @param idFurniture the id of the furniture.
   * @param purchasePrice the purchase price.
   * @param nextState the new state.
   */
  public void updatePurchasePrice(int idFurniture, double purchasePrice, String nextState,
      Date dateCarryFromClient) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET prix_achat =" + purchasePrice + ", m_etat = '"
          + nextState + "', date_emporte_mag = '" + dateCarryFromClient + "' WHERE id_meuble = "
          + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * update the status, the sell price and the special price of a furniture.
   * 
   * @param idFurniture is the id of the furniture.
   * @param sellPrice is the sell price of the furniture.
   * @param specialPrice is the special price of the furniture.
   * @param nextState is the new state.
   */
  public void updateSellPrice(int idFurniture, double sellPrice, double specialPrice,
      String nextState) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET prix_vente =" + sellPrice + ", m_etat = '" + nextState
          + "', prix_special = '" + specialPrice + "' WHERE id_meuble = " + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * update the date of a furniture what's just pass in the state ENMAG.
   * 
   * @param idFurniture the id of the furniture.
   * @param date the date to update.
   */
  @Override
  public void updateDateCarryToStore(int idFurniture, Date date) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET date_depot_en_mag ='" + date
          + "', m_etat = 'ENMAG' WHERE id_meuble = " + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Inject a new option in the DataBase.
   * 
   * @param option is the option to inject in the DataBase.
   */
  public void insertOption(Option option) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("INSERT INTO projet.options VALUES (DEFAULT, '" + option.getUserId()
          + "', '" + option.getFurnitureId() + "', '" + option.getDuration() + "', '"
          + option.getLimitDate() + "', '" + option.getIsCancel() + "', '" + option.getDaysLeft()
          + "') RETURNING id_option");
      if (rs.next()) {
        option.setId(rs.getInt("id_option"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * update the state of a furniture.
   * 
   * @param idFurniture is the id of the furniture.
   * @param nextState is the new state.
   */
  @Override
  public void updateFurnitureStatus(int idFurniture, String nextState) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET m_etat = '" + nextState + "' WHERE id_meuble = "
          + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * update the state of a furniture to RETIR, the sellPrice to NULL, the specialPrice to NULL and
   * the date of withdrawal to the date given in parameter.
   * 
   * @param idFurniture the furniture to update.
   * @param date the date of withdrawal.
   */
  @Override
  public void withdrawalFromSale(int idFurniture, Date date) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET m_etat = 'RETIR', prix_vente = NULL,"
          + " prix_special = NULL, date_retrait = '" + date + "' WHERE id_meuble = " + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * get all furnitures from the db.
   * 
   * @return a list of all furnitures.
   */
  @Override
  public List<FurnitureDTO> getAllFurnitures(boolean hasAdminPermission) throws DALErrorException {
    ResultSet rs = null;
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    try (Statement s = dals.getStatement()) {
      if (hasAdminPermission) {
        rs = s.executeQuery(
            "SELECT m.*, t.libelle FROM projet.meubles m, projet.types t WHERE m.type = t.id_type");
      } else {
        rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
            + "WHERE m.type = t.id_type AND (m.m_etat = 'ENVEN' OR m.m_etat = 'ENOPT')");
      }
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        Photo p = getPhoto(rs.getInt("photo_preferee"));
        f.setFavouritePhoto(p);
        furnitures.add(f);
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return furnitures;
  }

  /**
   * Get the photo with the id in parameter.
   * 
   * @param id the id of the photo.
   * @return the photo with the id in parameter
   * @throws DALErrorException when something related to the database has failed.
   */
  public Photo getPhoto(int id) throws DALErrorException {
    ResultSet rs = null;
    Photo photo = domainFactory.getPhoto();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT id_photo, photo, defilable FROM projet.photos WHERE id_photo = " + id);
      if (rs.next()) {
        photo.setId(rs.getInt("id_photo"));
        photo.setBytes(FileUtils.readFile(rs.getString("photo")));
        photo.setPath(rs.getString("photo"));
        photo.setScrollable(rs.getBoolean("defilable"));
        photo.setExtension(FilenameUtils.getExtension(photo.getPath()));
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } catch (IOException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      try {
        rs.close();
      } catch (SQLException e) {
        throw new DALErrorException(e);
      }
    }
    return photo;
  }

  /**
   * Get a list of photo of a furniture thanks to its id.
   * 
   * @param furniture the id of the furniture.
   * @return a list of photo of the furniture with the id in parameter.
   * @throws DALErrorException when something related to the database has failed.
   */
  public List<Photo> getAllPhotosOfAFurniture(int furniture) throws DALErrorException {
    ResultSet rs = null;
    List<Photo> photos = new ArrayList<Photo>();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT p.id_photo, p.photo, p.defilable FROM projet.photos p WHERE p.meuble = "
              + furniture);
      while (rs.next()) {
        Photo p = domainFactory.getPhoto();
        p.setId(rs.getInt("id_photo"));
        p.setBytes(FileUtils.readFile(rs.getString("photo")));
        p.setPath(rs.getString("photo"));
        p.setScrollable(rs.getBoolean("defilable"));
        p.setExtension(FilenameUtils.getExtension(p.getPath()));
        photos.add(p);
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } catch (IOException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      try {
        rs.close();
      } catch (SQLException e) {
        throw new DALErrorException(e);
      }
    }
    return photos;
  }


  /**
   * update an option to canceled.
   * 
   * @param idOption is the id of the option.
   * @param daysLeft is the number of day left.
   */
  public void updateRemoveOption(int idOption, int daysLeft) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.options SET jours_restants = '" + daysLeft
          + "', est_annulee = true WHERE id_option = " + idOption);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * update an option to not canceled.
   * 
   * @param opt is the option we need to update.
   */
  public void updateOption(Option opt) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("UPDATE projet.options SET duree = '" + opt.getDuration()
          + "', est_annulee = '" + opt.getIsCancel() + "',  date_limite = '" + opt.getLimitDate()
          + "' WHERE meuble = '" + opt.getFurnitureId() + "' AND  client = '" + opt.getUserId()
          + "' RETURNING id_option");
      if (rs.next()) {
        opt.setId(rs.getInt("id_option"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * get an option.
   * 
   * @param opt is the option we need to get.
   */
  public Option getOption(Option opt) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT o.* FROM projet.options o" + " WHERE meuble = '"
          + opt.getFurnitureId() + "' AND est_annulee = false");
      if (rs.next()) {
        opt.setId(rs.getInt("id_option"));
        opt.setDaysLeft(rs.getInt("jours_restants"));
        opt.setIsCancel(rs.getBoolean("est_annulee"));
        opt.setLimitDate(rs.getDate("date_limite"));
        opt.setDuration(rs.getInt("duree"));
        opt.setUserID(rs.getInt("client"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return opt;
  }

  /**
   * Get all furnitures filtred by price and type from the db.
   * 
   * @param isAdmin if the user is an admin or not.
   * @param minPrice is the minimum price wanted.
   * @param maxPrice is the maximum price wanted.
   * @param type is the type of furniture wanted.
   * 
   * @return a list of filtred furnitures.
   */
  @Override
  public List<FurnitureDTO> getFiltredFurnitures(boolean isAdmin, int minPrice, int maxPrice,
      int type) throws DALErrorException {
    ResultSet rs = null;
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    try (Statement s = dals.getStatement()) {
      if (isAdmin) {
        if (type == 0) {
          rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
              + " WHERE m.type = t.id_type AND m.prix_vente >= '" + minPrice
              + "' AND m.prix_vente <= '" + maxPrice + "'");
        } else {
          rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
              + "WHERE m.type = t.id_type AND m.type = " + type + " AND m.prix_vente >= '"
              + minPrice + "' AND m.prix_vente <= '" + maxPrice + "'");
        }
      } else {
        if (type == 0) {
          rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
              + "WHERE m.type = t.id_type AND (m.m_etat = 'ENVEN' OR m.m_etat = 'ENOPT')"
              + " AND m.prix_vente >= '" + minPrice + "' AND m.prix_vente <= '" + maxPrice + "'");
        } else {
          rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
              + "WHERE m.type = t.id_type AND "
              + "(m.m_etat = 'ENVEN' OR m.m_etat = 'ENOPT') AND m.type = " + type
              + " AND m.prix_vente >= '" + minPrice + "' AND m.prix_vente <= '" + maxPrice + "'");
        }
      }
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        f.setFavouritePhoto(getPhoto(rs.getInt("photo_preferee")));
        furnitures.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return furnitures;
  }


  /**
   * Get all furnitures filtred by type from the db.
   * 
   * @param type is the type of furniture wanted.
   * 
   * @return a list of filtred furnitures.
   */
  @Override
  public List<FurnitureDTO> getFiltredFurnituresQuidam(int type) throws DALErrorException {
    ResultSet rs = null;
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    try (Statement s = dals.getStatement()) {
      if (type == 0) {
        rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
            + "WHERE m.type = t.id_type AND (m.m_etat = 'ENVEN' OR m.m_etat = 'ENOPT')");
      } else {
        rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
            + "WHERE m.type = t.id_type AND (m.m_etat = 'ENVEN' OR m.m_etat = 'ENOPT')"
            + " AND m.type = " + type);
      }
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        f.setFavouritePhoto(getPhoto(rs.getInt("photo_preferee")));
        furnitures.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return furnitures;
  }


  /**
   * Allows to get all furnitures which have a similitude with the keyword, in the type, the
   * purchaser's name or the purchaser's username.
   * 
   * @param word is the keyword used for the research.
   * @return a List of all users found.
   */
  public List<FurnitureDTO> getfurnituresByResearch(String word) throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();

    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT m.*, t.libelle FROM projet.meubles m, projet.utilisateurs u, projet.types t "
              + "WHERE u.id_utilisateur = m.client"
              + " AND m.type = t.id_type AND (LOWER(t.libelle) LIKE LOWER('%" + word + "%')"
              + "OR LOWER(u.pseudo) LIKE LOWER('%" + word + "%')" + "OR LOWER(u.nom) LIKE LOWER('%"
              + word + "%'))");
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        f.setSeller(domainFactory.getUser());
        f.getSeller().setId(rs.getInt("client"));
        furnitures.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return furnitures;
  }

  /**
   * Allows to get all tags agreeing to tags in db.
   *
   * @return a list of tags.
   */
  @Override
  public List<String> getTags() throws DALErrorException {
    List<String> tags = new ArrayList<String>();

    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT libelle FROM projet.types");
      while (rs.next()) {
        tags.add(rs.getString("libelle"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return tags;
  }

  /**
   * insert the path of a photo for a furniture with the id in parameter.
   * 
   * @param photo the photo to insert.
   * @param idFurniture the id of the furniture.
   * @throws DALErrorException when something related to the database has failed.
   */
  public Photo insertPhoto(Photo photo, int idFurniture) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("INSERT INTO projet.photos VALUES(DEFAULT, '" + photo.getPath() + "', "
          + idFurniture + ", false) RETURNING id_photo");
      if (rs.next()) {
        photo.setId(rs.getInt("id_photo"));
        photo.setExtension(FilenameUtils.getExtension(photo.getPath()));
        photo.setScrollable(false);
      }

    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return photo;
  }



  /**
   * Get the id of the type from its label.
   * 
   * @param label the label of the type.
   * @return id the id of the type.
   */
  @Override
  public int getTypeFromLabel(String label) throws DALErrorException {
    ResultSet rs = null;
    int id = 0;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT id_type FROM projet.types WHERE libelle = '" + label + "'");
      if (rs.next()) {
        id = rs.getInt("id_type");
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      try {
        rs.close();
      } catch (SQLException e) {
        throw new DALErrorException(e);
      }
    }
    return id;
  }


  /**
   * Insert the furniture in parameter.
   * 
   * @param furniture the furniture to insert
   * @param visitId the id of the visit linked to the furniture.
   * @return furniture the furniture inserted.
   */
  @Override
  public FurnitureDTO insertFurniture(FurnitureDTO furniture, int visitId)
      throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      String description = furniture.getDescription();
      if (furniture.getDescription().contains("'")) {
        description = furniture.getDescription().replace("'", "''");
      }
      rs = s.executeQuery(
          "INSERT INTO projet.meubles VALUES(DEFAULT, " + getTypeFromLabel(furniture.getType())
              + ", '" + description + "', 'PROPO', " + furniture.getSeller().getId()
              + ", -1, NULL, NULL, NULL, -1, -1, " + visitId + ", NULL) RETURNING id_meuble");
      if (rs.next()) {
        furniture.setId(rs.getInt("id_meuble"));
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (SQLException e) {
        throw new DALErrorException(e);
      }
    }
    return furniture;

  }


  /**
   * Update the favorite photo of a furniture.
   * 
   * @param furniture the furniture which will have its favorite photo changed.
   * @param photoId the id of the new favorite photo.
   */
  @Override
  public void updateFavoritePhoto(FurnitureDTO furniture, int photoId) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET photo_preferee = " + photoId + " WHERE id_meuble = "
          + furniture.getId());
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }


  /**
   * Allows to add a sale in db.
   * 
   * @param furniture the furniture sold.
   */
  @Override
  public void addSale(FurnitureDTO furniture) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      if (furniture.getPurchaser() != null) {
        s.execute("INSERT INTO projet.ventes VALUES(" + furniture.getId() + ", DATE('"
            + furniture.getDateSale() + "'), " + furniture.getPurchaser().getId() + ")");
      } else { // shop's sale (without account)
        s.execute("INSERT INTO projet.ventes VALUES(" + furniture.getId() + ", DATE('"
            + furniture.getDateSale() + "'), NULL)");
      }

    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }


  /**
   * Allows to update the furniture sold.
   * 
   * @param furniture the furniture to update.
   */
  @Override
  public void sellFurniture(FurnitureDTO furniture) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.executeQuery("UPDATE projet.meubles SET m_etat = 'VENDU', prix_special = "
          + furniture.getSpecialPrice() + " WHERE id_meuble = " + furniture.getId()
          + " RETURNING id_meuble");
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }


  /**
   * Allows to get all furnitures associated to a visit.
   * 
   * @param idVisit the id of the visit which contain all furnitures that will be retrieved.
   * @return A list of furniture contain in the visit with the id in parameter.
   */
  @Override
  public List<FurnitureDTO> getAllFurnituresOfAVisit(int idVisit) throws DALErrorException {
    ResultSet rs = null;
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t WHERE "
          + idVisit + " = m.visite AND t.id_type = m.type");
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        f.setId(rs.getInt("id_meuble"));
        f.setState("m_etat");
        f.setType(rs.getString("libelle"));
        f.setDescription(rs.getString("description"));
        UserDTO u = domainFactory.getUser();
        u.setId(rs.getInt("client"));
        f.setSeller(u);
        f.setFavouritePhoto(getPhoto(rs.getInt("photo_preferee")));
        furnitures.add(f);
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          throw new DALErrorException(e);
        }
      }
    }
    return furnitures;
  }

  /**
   * Allows to change the scrollable status of a photo.
   * 
   * @param id the id of the photo to update.
   * @param scrollable the current scrollable status of the photo.
   * 
   */
  @Override
  public void changeScrollable(int id, boolean scrollable) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.photos SET defilable = " + !scrollable + " WHERE id_photo = " + id);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Update a furniture to PASCO state.
   * 
   * @param idVisit is the id of the visit cancelled.
   */
  @Override
  public void cancelVisitFurniture(int idVisit) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET m_etat = 'PASCO' WHERE m_etat = 'PROPO' AND visite = "
          + idVisit);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Allows to change the favourite photo of a furniture.
   * 
   * @param idFurniture the id of the furniture.
   * @param idPhoto the id of the new favourite photo.
   * 
   */
  @Override
  public void changeFavouritePhoto(int idFurniture, int idPhoto) {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.meubles SET photo_preferee = " + idPhoto + " WHERE id_meuble = "
          + idFurniture);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Update the furniture given to PASCO in DB.
   * 
   * @param furniture the furniture to update.
   * 
   */
  @Override
  public void notSuitable(FurnitureDTO furniture) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute(
          "UPDATE projet.meubles SET m_etat = 'PASCO' WHERE id_meuble = " + furniture.getId());
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }


  /**
   * Allows to get all options which are not canceled.
   * 
   * @return the list of all options not canceled.
   */
  @Override
  public List<Option> getAllOptionsNotCanceled() throws DALErrorException {
    ResultSet rs = null;
    List<Option> options = new ArrayList<Option>();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.options WHERE est_annulee = false");
      while (rs.next()) {
        Option o = domainFactory.getOption();
        o.setDaysLeft(rs.getInt("jours_restants"));
        o.setDuration(rs.getInt("duree"));
        o.setFurnitureId(rs.getInt("meuble"));
        o.setId(rs.getInt("id_option"));
        o.setIsCancel(rs.getBoolean("est_annulee"));
        o.setLimitDate(rs.getDate("date_limite"));
        o.setUserID(rs.getInt("client"));
        options.add(o);
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          throw new DALErrorException(e);
        }
      }
    }
    return options;
  }

  /**
   * Add a type to the db.
   * 
   * @param type a string which is the new libelle.
   * @return the id of the type.
   */
  @Override
  public int addType(String type) {
    ResultSet rs = null;
    int typeId = 0;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "INSERT INTO projet.types VALUES (DEFAULT,'" + type + "') RETURNING id_type");
      if (rs.next()) {
        typeId = rs.getInt("id_type");
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return typeId;
  }
}
