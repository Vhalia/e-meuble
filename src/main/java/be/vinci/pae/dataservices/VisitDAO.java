package be.vinci.pae.dataservices;

import java.util.List;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.DALErrorException;

public interface VisitDAO {

  VisitDTO insertVisitRequest(VisitDTO visit);

  List<VisitDTO> getAllVisits();

  VisitDTO getVisit(int idVisit);

  VisitDTO cancelAVisit(VisitDTO visit) throws DALErrorException;

  void confirmVisit(VisitDTO visit);

  List<VisitDTO> getVisitsOfAUser(int id);

}
