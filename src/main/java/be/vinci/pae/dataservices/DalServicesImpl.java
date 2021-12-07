package be.vinci.pae.dataservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp2.BasicDataSource;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.utils.Config;
import jakarta.inject.Singleton;

/**
 * Class which represents the implementation of the interface DalServices.
 */
@Singleton
public class DalServicesImpl implements DalBackendServices, DalServices {

  static BasicDataSource bds;
  static ThreadLocal<Connection> tl;
  static ThreadLocal<Integer> connectionIsClosable;


  // initialize static attributes of DalServices, create the connection pool and init the connection
  // to the db.
  static {
    bds = new BasicDataSource();
    bds.setDriverClassName(Config.getProperty("driver"));
    bds.setUrl(Config.getProperty("db"));
    bds.setUsername(Config.getProperty("user"));
    bds.setPassword(Config.getProperty("password"));
    bds.setInitialSize(Config.getIntProperty("initialSizeConnections"));
    bds.setMaxTotal(Config.getIntProperty("maxTotalConnections"));

    tl = new ThreadLocal<Connection>();
    connectionIsClosable = new ThreadLocal<Integer>();
  }

  /**
   * Allows to determine if the next call of close connection, rollback transaction, commit
   * transaction will close the connection. When calling this methods it will increment a counter
   * link to the thread. A connection will be close only and only if the counter is equal to 0.
   */
  public void up() throws DALErrorException {
    Connection conn = tl.get();
    if (conn == null) {
      throw new DALErrorException("Error: current thread has no connection."
          + " You should start a transaction or get a Statement first");
    }
    int i = connectionIsClosable.get();
    i++;
    connectionIsClosable.set(i);
  }

  /**
   * Allows to determine if the next call of close connection, rollback transaction, commit
   * transaction will close the connection. When calling this methods it will decrement a counter
   * link to the thread. A connection will be close only and only if the counter is equal to 0.
   */
  public void down() throws DALErrorException {
    Connection conn = tl.get();
    if (conn == null) {
      throw new DALErrorException("Error: current thread has no connection."
          + " You should start a transaction or get a Statement first");
    }
    int i = connectionIsClosable.get();
    if (i == 0) {
      return;
    }
    i--;
    connectionIsClosable.set(i);
  }

  /**
   * Allows to get a Statement in order to execute a query.
   *
   * @return A Statement related to the requested query.
   * 
   */
  public Statement getStatement() throws DALErrorException {
    Connection conn = tl.get();
    Statement s = null;
    try {
      if (conn == null) {
        conn = bds.getConnection();
        tl.set(conn);
        connectionIsClosable.set(0);
      }
      s = conn.createStatement();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }
    return s;
  }

  /**
   * start a transaction.
   */
  @Override
  public void startTransaction() throws DALErrorException {
    try {
      Connection conn = tl.get();
      if (conn == null) {
        conn = bds.getConnection();
        connectionIsClosable.set(0);
      }
      // wait a commit to save in db
      conn.setAutoCommit(false);
      tl.set(conn);
    } catch (SQLException e) {
      throw new DALErrorException(e);
    }

  }

  /**
   * commit and close a transaction.
   * 
   * @return True if the connection has commit and closed, false otherwise. This methods return
   *         false when the connection has been set to not closable thanks to the method up.
   */
  @Override
  public boolean commitTransaction() throws DALErrorException {
    Connection conn = tl.get();
    if (conn == null) {
      throw new InternalError("Error: the current thread has no connection to commit");
    }
    if (connectionIsClosable.get() != 0) {
      return false;
    }
    try {
      conn.commit();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new DALErrorException(e);
      } finally {
        tl.remove();
      }
    }
    return true;
  }


  /**
   * rollback and close a transaction.
   * 
   * @return True if the connection has been roll back and closed, false otherwise. This methods
   *         return false when the connection has been set to not closable thanks to the method up.
   */
  @Override
  public boolean rollbackTransaction() throws DALErrorException {
    Connection conn = tl.get();
    if (conn == null) {
      throw new InternalError("Error: the current thread has no connection to rollback");
    }
    if (connectionIsClosable.get() != 0) {
      return false;
    }
    try {
      conn.rollback();
    } catch (SQLException e) {
      throw new SomethingWentWrongException(e);
    } finally {
      try {
        conn.close();
      } catch (SQLException e) {
        throw new DALErrorException(e);
      } finally {
        tl.remove();
      }
    }
    return true;
  }

  /**
   * Allows to release a connection from a thread.
   * 
   * @return True if the connection has been closed, false otherwise. This methods return false when
   *         the connection has been set to not closable thanks to the method up.
   */
  @Override
  public boolean closeConnection() throws DALErrorException {
    Connection conn = tl.get();
    if (conn == null) {
      throw new InternalError("Error: the current thread has no connection to close");
    }
    if (connectionIsClosable.get() != 0) {
      return false;
    }
    try {
      conn.close();
    } catch (SQLException e) {
      throw new DALErrorException(e);
    } finally {
      tl.remove();
    }
    return true;
  }

}
