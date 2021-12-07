package be.vinci.pae.uc;

import java.io.InputStream;
import java.util.List;
import be.vinci.pae.domain.VisitDTO;

public interface VisitUCC {

  VisitDTO createVisitRequest(VisitDTO visit, List<InputStream> files, boolean createFakeClient);

  List<VisitDTO> getAllVisits();

  VisitDTO getVisit(int id);

  VisitDTO confirmVisit(VisitDTO visit);

  VisitDTO cancelAVisit(VisitDTO visit);

  List<VisitDTO> getVisitsOfAUser(int id);

}
