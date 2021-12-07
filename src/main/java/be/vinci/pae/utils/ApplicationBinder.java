package be.vinci.pae.utils;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import be.vinci.pae.dataservices.DalBackendServices;
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
public class ApplicationBinder extends AbstractBinder {

  @Override
  protected void configure() {

    bind(DomainFactoryImpl.class).to(DomainFactory.class).in(Singleton.class);

    // userClasses
    bind(UserDAOImpl.class).to(UserDAO.class).in(Singleton.class);
    bind(UserUCCImpl.class).to(UserUCC.class).in(Singleton.class);


    // furnitureClasses
    bind(FurnitureDAOImpl.class).to(FurnitureDAO.class).in(Singleton.class);
    bind(FurnitureUCCImpl.class).to(FurnitureUCC.class).in(Singleton.class);

    bind(DalServicesImpl.class).to(DalBackendServices.class).to(DalServices.class)
        .in(Singleton.class);

    // visitClass
    bind(VisitUCCImpl.class).to(VisitUCC.class).in(Singleton.class);
    bind(VisitDAOImpl.class).to(VisitDAO.class).in(Singleton.class);
  }

}
