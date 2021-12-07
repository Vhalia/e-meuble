package be.vinci.pae.dataservices;

import be.vinci.pae.exceptions.DALErrorException;
import jakarta.inject.Singleton;

/**
 * Represents the interface for the type DalServices. It contains methods related to transactions.
 */
@Singleton
public interface DalServices {

  void startTransaction() throws DALErrorException;

  boolean commitTransaction() throws DALErrorException;

  boolean rollbackTransaction() throws DALErrorException;

  boolean closeConnection() throws DALErrorException;

  void up() throws DALErrorException;

  void down() throws DALErrorException;

}
