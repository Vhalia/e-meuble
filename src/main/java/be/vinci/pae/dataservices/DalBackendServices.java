package be.vinci.pae.dataservices;

import java.sql.Statement;
import be.vinci.pae.exceptions.DALErrorException;
import jakarta.inject.Singleton;

/**
 * Represents the interface for the type DalServices. It contains methods accessible only in DAL
 * layer.
 */
@Singleton
public interface DalBackendServices {

  Statement getStatement() throws DALErrorException;
}
