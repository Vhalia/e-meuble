package be.vinci.pae.dataservices;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.DALErrorException;
import jakarta.inject.Inject;

public class VisitDAOImpl implements VisitDAO {

  @Inject
  DalBackendServices dals;

  @Inject
  DomainFactory domaineFactory;

  @Override
  public VisitDTO insertVisitRequest(VisitDTO visit) {
    ResultSet rs = null;
    try (Statement s = dals.getStatement()) {
      String query = "INSERT INTO projet.visites VALUES(DEFAULT, '" + visit.getRequestDate()
          + "', '" + visit.getUsersTimeSlot() + "'," + visit.getStorageAddress().getId() + ", '"
          + visit.getVisitDateTime() + "', '" + visit.getState() + "', " + visit.getClient().getId()
          + ", " + null + ") RETURNING id_visite";
      query = query.replace("'null'", "null");
      rs = s.executeQuery(query);
      if (rs.next()) {
        visit.setId(rs.getInt("id_visite"));
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
    return visit;
  }

  /**
   * Allows to retrieve all visits.
   * 
   * @return list of all visits.
   */
  @Override
  public List<VisitDTO> getAllVisits() {
    ResultSet rs = null;
    List<VisitDTO> visits = new ArrayList<VisitDTO>();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery(
          "SELECT v.id_visite, v.date_demande, v.date_et_heure_visite, v.mot_annulation,"
              + " v.etat_visite, u.pseudo FROM projet.visites v, projet.utilisateurs u "
              + "WHERE v.client = u.id_utilisateur");
      while (rs.next()) {
        VisitDTO v = domaineFactory.getVisit();
        UserDTO u = domaineFactory.getUser();
        setListByResultSet(u, v, rs);
        visits.add(v);
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
    return visits;
  }

  /**
   * Allows to retrieve a visit according to the id in parameter.
   * 
   * @param idVisit the id of the visit to retrieve.
   * @return the visit with the id in parameter.
   */
  @Override
  public VisitDTO getVisit(int idVisit) {
    ResultSet rs = null;
    VisitDTO visit = null;
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT id_visite, date_demande, plage_horaire_client, etat_visite,"
          + " mot_annulation, client, adresse_entreposage FROM projet.visites WHERE id_visite = "
          + idVisit);
      if (rs.next()) {
        visit = domaineFactory.getVisit();
        visit.setId(rs.getInt("id_visite"));
        visit.setRequestDate(rs.getDate("date_demande"));
        visit.setUsersTimeSlot(rs.getString("plage_horaire_client"));
        visit.setState(rs.getString("etat_visite"));
        visit.setCancellationNote(rs.getString("mot_annulation"));
        UserDTO user = domaineFactory.getUser();
        user.setId(rs.getInt("client"));
        visit.setClient(user);
        Address adr = domaineFactory.getAddress();
        adr.setId(rs.getInt("adresse_entreposage"));
        visit.setStorageAddress(adr);
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
    return visit;
  }

  /**
   * Update the visit given in param in the db to be confirmed.
   * 
   * @param visit to update.
   */
  @Override
  public void confirmVisit(VisitDTO visit) {

    Timestamp timestamp = Timestamp.valueOf(visit.getVisitDateTime());

    try (Statement s = dals.getStatement()) {
      s.execute("UPDATE projet.visites SET date_et_heure_visite = '" + timestamp
          + "', etat_visite = 'CONF' WHERE id_visite = " + visit.getId());
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
  }



  /**
   * update a visit from the database to cancelled.
   * 
   * @param visit is the visit to cancel.
   * 
   * @return the cancelled visit.
   */
  @Override
  public VisitDTO cancelAVisit(VisitDTO visit) throws DALErrorException {
    try (Statement s = dals.getStatement()) {
      String cancellationNote = visit.getCancellationNote().replace("'", "''");
      s.execute("UPDATE projet.visites SET mot_annulation = '" + cancellationNote
          + "', etat_visite = 'ANN' WHERE id_visite = " + visit.getId());
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    visit.setState("ANN");
    return visit;
  }


  /**
   * Allows to get the visits of a user in param from the db.
   * 
   * @param id the id of the user
   * @return a list of visits.
   */
  @Override
  public List<VisitDTO> getVisitsOfAUser(int id) {
    ResultSet rs = null;
    List<VisitDTO> visits = new ArrayList<VisitDTO>();
    try (Statement s = dals.getStatement()) {
      rs = s.executeQuery("SELECT v.id_visite, v.date_demande, v.date_et_heure_visite, "
          + " v.etat_visite, u.pseudo, v.mot_annulation "
          + "FROM projet.visites v, projet.utilisateurs u"
          + " WHERE v.client = u.id_utilisateur AND v.client = " + id);
      while (rs.next()) {
        VisitDTO v = domaineFactory.getVisit();
        UserDTO u = domaineFactory.getUser();
        setListByResultSet(u, v, rs);
        visits.add(v);
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
    return visits;
  }

  private static VisitDTO setListByResultSet(UserDTO u, VisitDTO v, ResultSet rs) {
    try {
      v.setId(rs.getInt("id_visite"));
      v.setRequestDate(rs.getDate("date_demande"));
      Timestamp dateAndHourVisit = rs.getTimestamp("date_et_heure_visite");
      if (dateAndHourVisit != null) {
        v.setVisitDateTime(dateAndHourVisit.toLocalDateTime());
      }
      if (rs.getString("mot_annulation") != null) {
        v.setCancellationNote(rs.getString("mot_annulation"));
      }
      v.setState(rs.getString("etat_visite"));
      u.setUserName(rs.getString("pseudo"));
      v.setClient(u);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return v;
  }


}
