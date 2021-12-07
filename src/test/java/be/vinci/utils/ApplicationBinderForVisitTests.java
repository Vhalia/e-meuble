package be.vinci.utils;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.mockito.Mockito;
import be.vinci.pae.dataservices.DalServices;
import be.vinci.pae.dataservices.DalServicesImpl;
import be.vinci.pae.dataservices.FurnitureDAO;
import be.vinci.pae.dataservices.FurnitureDAOImpl;
import be.vinci.pae.dataservices.UserDAO;
import be.vinci.pae.dataservices.UserDAOImpl;
import be.vinci.pae.dataservices.VisitDAO;
import be.vinci.pae.dataservices.VisitDAOImpl;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.DomainFactoryImpl;
import be.vinci.pae.uc.FurnitureUCC;
import be.vinci.pae.uc.FurnitureUCCImpl;
import be.vinci.pae.uc.UserUCC;
import be.vinci.pae.uc.UserUCCImpl;
import be.vinci.pae.uc.VisitUCC;
import be.vinci.pae.uc.VisitUCCImpl;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApplicationBinderForVisitTests extends AbstractBinder {

  @Override
  protected void configure() {
    bind(Mockito.mock(FurnitureDAOImpl.class)).to(FurnitureDAO.class);
    bind(Mockito.mock(UserDAOImpl.class)).to(UserDAO.class);
    bind(Mockito.mock(DalServicesImpl.class)).to(DalServices.class);
    bind(Mockito.mock(VisitDAOImpl.class)).to(VisitDAO.class);
    bind(Mockito.mock(UserUCCImpl.class)).to(UserUCC.class);
    bind(Mockito.mock(FurnitureUCCImpl.class)).to(FurnitureUCC.class);

    bind(DomainFactoryImpl.class).to(DomainFactory.class).in(Singleton.class);
    bind(VisitUCCImpl.class).to(VisitUCC.class).in(Singleton.class);

  }

}
