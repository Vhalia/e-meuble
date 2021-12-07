package be.vinci.pae.dataservices;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.DALErrorException;
import jakarta.inject.Inject;

/**
 * Class which represents the implementation of UserDAO.
 */
public class UserDAOImpl implements UserDAO {

  @Inject
  private DomainFactory domainFactory;

  @Inject
  private DalBackendServices dals;

  @Inject
  private FurnitureDAO furnitureDAO;

  /**
   * Allows to retrieves a User present in the database thanks to his username.
   * 
   * @param userName the username of the user which will be retrieve from the database.
   * @return the user found in the database.
   */
  public UserDTO getUser(String userName) throws DALErrorException {
    UserDTO us = null;
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.utilisateurs WHERE pseudo = '" + userName + "'");
      us = setUserFields(rs, true);
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return us;

  }

  /**
   * Allows to retrieves a User present in the database thanks to his id.
   * 
   * @param id the id of the user which will be retrieve from the database.
   * @return the user found in the database.
   */
  @Override
  public UserDTO getUser(int id) throws DALErrorException {
    UserDTO user = null;
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.utilisateurs WHERE id_utilisateur = " + id);
      user = setUserFields(rs, true);
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return user;
  }

  /**
   * Allows to retrieves an address present in the database thanks to its id.
   * 
   * @param id the id of the address which will be retrieve from the database.
   * @return the address found in the database.
   */
  @Override
  public Address getAddress(int id) throws DALErrorException {
    Address adr = null;
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.adresses WHERE id_adresse = " + id);
      if (rs.next()) {
        adr = domainFactory.getAddress();
        adr.setId(rs.getInt("id_adresse"));
        adr.setStreet(rs.getString("rue"));
        adr.setNbr(rs.getString("numero"));
        adr.setBox(rs.getString("boite"));
        adr.setPostalCode(rs.getString("code_postal"));
        adr.setCommune(rs.getString("commune"));
        adr.setCountry(rs.getString("pays"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return adr;
  }

  /**
   * Allows to set all the fields of an UserDTO object when receiving data from the database.
   * 
   * @param rs the result set of the query previously executed.
   * @param callNext boolean which determines if you want to call the next() method on the rs given
   *        in parameter.
   * @return a fulfilled userDTO.
   */
  private UserDTO setUserFields(ResultSet rs, boolean callNext) throws DALErrorException {
    UserDTO user = null;
    try {
      if (!callNext || rs.next()) {
        user = this.domainFactory.getUser();
        user.setId(rs.getInt("id_utilisateur"));
        user.setUserName(rs.getString("pseudo"));
        user.setPassword(rs.getString("mot_de_passe"));
        user.setName(rs.getString("nom"));
        user.setSurname(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("u_role"));
        user.setRegistrationValidated(rs.getBoolean("inscription_validee"));
        user.setRegistrationDate(rs.getDate("date_inscription"));
        user.setNbrFurnituresBought(rs.getInt("nbr_meubles_achetes"));
        user.setNbrFurnituresSold(rs.getInt("nbr_meubles_vendus"));
        user.setFakeClient(rs.getBoolean("client_factice"));
        user.setAddress(getAddress(rs.getInt("adresse")));
      }
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return user;
  }

  /**
   * Allows to check if an user with the email in parameter already exists.
   * 
   * @param email the email to check.
   * @return false if the email is already associated to an user and true otherwise.
   */
  public boolean checkEmail(String email) throws DALErrorException {
    email = StringEscapeUtils.escapeHtml4(email);
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.utilisateurs WHERE email = '" + email + "'");
      if (rs.next()) {
        return false;
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return true;
  }

  /**
   * Allows to check if an user with the userName in parameter already exists.
   * 
   * @param userName the userName to check.
   * @return false if the userName is already associated to an user and true otherwise.
   */
  public boolean checkUserName(String userName) throws DALErrorException {
    userName = StringEscapeUtils.escapeHtml4(userName);
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.utilisateurs WHERE pseudo = '" + userName + "'");
      if (rs.next()) {
        return false;
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return true;
  }

  /**
   * Inject a user in the DataBase.
   *
   * @param user the new user which will be injected into the db.
   */
  public void createUser(UserDTO user) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      Integer adrId = -1;
      if (user.getAddress() == null) {
        adrId = null;
      } else {
        adrId = user.getAddress().getId();
      }
      user.setRegistrationDate(Date.valueOf(LocalDate.now()));
      String query = "INSERT INTO projet.utilisateurs VALUES (DEFAULT,'" + user.getUserName()
          + "','" + user.getPassword() + "','" + user.getName() + "','" + user.getSurname() + "','"
          + user.getEmail() + "','" + user.getRole() + "'," + user.isRegistrationValidated() + ","
          + adrId + ",'" + user.getRegistrationDate() + "',DEFAULT,DEFAULT," + user.isFakeClient()
          + ") RETURNING id_utilisateur";
      query = query.replace("'null'", "null");
      rs = s.executeQuery(query);
      if (rs.next()) {
        user.setId(rs.getInt("id_utilisateur"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Inject a new address in the dataBase.
   * 
   * @param adr the new address which will be injected into the db.
   */
  @Override
  public void createAddress(Address adr) throws DALErrorException {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      String box = adr.getBox();
      if (box != null) {
        box = "'" + box + "'";
      } else {
        box = "NULL";
      }
      rs = s.executeQuery("INSERT INTO projet.adresses VALUES (DEFAULT,'" + adr.getStreet() + "','"
          + adr.getNbr() + "'," + box + ",'" + adr.getPostalCode() + "','" + adr.getCommune()
          + "','" + adr.getCountry() + "') RETURNING id_adresse");
      if (rs.next()) {
        adr.setId(rs.getInt("id_adresse"));
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Update the role of the user with the id in parameter.
   * 
   * @param roleRequested the new role of the user.
   * @param id the is of the user.
   */
  @Override
  public void updateRole(String roleRequested, int id) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.utilisateurs SET u_role = '" + roleRequested
          + "', inscription_validee = true WHERE id_utilisateur = " + id);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }

  /**
   * Get from the database all the users who are not validated.
   * 
   * @return a list of user not validated.
   */
  @Override
  public List<UserDTO> getUsersNotValidated() throws DALErrorException {
    List<UserDTO> usersNotValidated = new ArrayList<UserDTO>();
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT * FROM projet.utilisateurs WHERE inscription_validee = false");
      while (rs.next()) {
        UserDTO u = setUserFields(rs, false);
        usersNotValidated.add(u);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return usersNotValidated;
  }

  /**
   * Allows to refuse the registration of an user by setting his role as "CLI" (client) and
   * inscription_validee as true.
   * 
   * @param id the id of the user who's registration will be refused.
   */
  @Override
  public void updateUserRegistrationRefused(int id) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.utilisateurs SET u_role = 'CLI'"
          + ", inscription_validee = true WHERE id_utilisateur = " + id);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

  }

  /**
   * Allows to get all the furnitures with an option set by the user with the id in parameter.
   * 
   * @param id the id of the user.
   * @return furnituresOption a list of furniture with an option.
   */
  @Override
  public List<FurnitureDTO> getFurnituresWithAnOption(int id) throws DALErrorException {
    List<FurnitureDTO> furnituresOption = new ArrayList<FurnitureDTO>();
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT m.*, t.libelle FROM projet.meubles m, projet.options o, projet.types t "
              + "WHERE m.m_etat = 'ENOPT' AND o.meuble = m.id_meuble AND o.client = " + id
              + " AND o.est_annulee = FALSE AND m.type = t.id_type");
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        Photo p = furnitureDAO.getPhoto(rs.getInt("photo_preferee"));
        f.setFavouritePhoto(p);
        furnituresOption.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return furnituresOption;
  }

  /**
   * Allows to get all the furnitures sold by the user with the id in parameter.
   * 
   * @param id the id of the user.
   * @return soldFurnitures a list of furnitures sold by the user.
   */
  @Override
  public List<FurnitureDTO> getSoldFurnitures(int id) throws DALErrorException {
    List<FurnitureDTO> soldFurnitures = new ArrayList<FurnitureDTO>();
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT m.*, t.libelle FROM projet.meubles m, projet.types t "
          + "WHERE m.type = t.id_type AND m.client = " + id);
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        Photo p = furnitureDAO.getPhoto(rs.getInt("photo_preferee"));
        f.setFavouritePhoto(p);
        soldFurnitures.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return soldFurnitures;
  }

  /**
   * Allows to get all the furnitures bought by the user with the id in parameter.
   * 
   * @param id the id of the user.
   * @return boughtFurnitures a list of furnitures bought by the user.
   */
  @Override
  public List<FurnitureDTO> getBoughtFurnitures(int id) throws DALErrorException {
    List<FurnitureDTO> boughtFurnitures = new ArrayList<FurnitureDTO>();
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT m.*, t.libelle FROM projet.meubles m, projet.types t, projet.ventes v"
              + " WHERE m.type = t.id_type AND m.id_meuble = v.id_meuble AND v.id_client = " + id);
      while (rs.next()) {
        FurnitureDTO f = domainFactory.getFurniture();
        FurnitureDAO.setAttributes(rs, f);
        Photo p = furnitureDAO.getPhoto(rs.getInt("photo_preferee"));
        f.setFavouritePhoto(p);
        boughtFurnitures.add(f);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return boughtFurnitures;
  }


  /**
   * Get the address of the user with the id given in parameter.
   * 
   * @param idUser the id of the user.
   * @return address the address of the user.
   */
  @Override
  public Address getAddressFromUserId(int idUser) throws DALErrorException {
    ResultSet rs = null;
    Address adr = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT a.* FROM projet.adresses a, projet.utilisateurs u WHERE u.id_utilisateur = "
              + idUser + " AND a.id_adresse = u.adresse");
      if (rs.next()) {
        adr = domainFactory.getAddress();
        adr.setId(rs.getInt("id_adresse"));
        adr.setStreet(rs.getString("rue"));
        adr.setNbr(rs.getString("numero"));
        adr.setBox(rs.getString("boite"));
        adr.setPostalCode(rs.getString("code_postal"));
        adr.setCommune(rs.getString("commune"));
        adr.setCountry(rs.getString("pays"));
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
    return adr;
  }

  /**
   * Allows to get all users who have a similitude with the keyword, in the name, the username, the
   * commune or the postal code.
   * 
   * @param word is the keyword used for the research.
   * @return a List of all users found.
   */
  @Override
  public List<UserDTO> getUsersByResearch(String word) throws DALErrorException {
    List<UserDTO> users = new ArrayList<UserDTO>();

    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT u.* FROM projet.utilisateurs u, projet.adresses a "
          + "WHERE u.adresse = a.id_adresse AND (LOWER(u.pseudo) LIKE LOWER('%" + word + "%')"
          + "OR LOWER(u.nom) LIKE LOWER('%" + word + "%')" + "OR  LOWER(a.commune) LIKE LOWER('%"
          + word + "%')" + "OR  LOWER(a.code_postal) LIKE LOWER('%" + word + "%'))");
      while (rs.next()) {
        UserDTO u = setUserFields(rs, false);
        users.add(u);
      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return users;
  }


  /**
   * Allows to get all tags agreeing to users in db.
   * 
   * @return a tags'list.
   */
  @Override
  public List<String> getTags() throws DALErrorException {
    List<String> tags = new ArrayList<String>();

    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT u.nom, u.pseudo, a.code_postal, a.commune"
          + " FROM projet.utilisateurs u, projet.adresses a " + "WHERE u.adresse = a.id_adresse");
      while (rs.next()) {
        if (!tags.contains(rs.getString("nom"))) {
          tags.add(rs.getString("nom"));
        }
        if (!tags.contains(rs.getString("pseudo"))) {
          tags.add(rs.getString("pseudo"));
        }
        if (!tags.contains(rs.getString("code_postal"))) {
          tags.add(rs.getString("code_postal"));
        }
        if (!tags.contains(rs.getString("commune"))) {
          tags.add(rs.getString("commune"));
        }


      }
      rs.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

    return tags;
  }

  /**
   * Method which increment the number of furnitures sold.
   * 
   * @param userId is the id of the user.
   */
  @Override
  public void incrementNbrFurnituresSold(int userId) {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.utilisateurs SET nbr_meubles_vendus = nbr_meubles_vendus + 1 "
          + "WHERE id_utilisateur = " + userId);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

  }


  /**
   * Method which increment the number of furnitures purchased.
   * 
   * @param userId is the id of the user.
   */
  @Override
  public void incrementNbrFurnituresPurchased(int userId) {
    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.utilisateurs SET nbr_meubles_achetes = nbr_meubles_achetes + 1"
          + " WHERE id_utilisateur = " + userId);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

  }
}
