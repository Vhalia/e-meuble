package be.vinci.pae.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class that gives services related to properties files.
 */
public class Config {
  private static Properties props;

  /**
   * Allows to load a file.
   * 
   * @param file the file that will be loaded.
   */
  public static void load(String file) {
    props = new Properties();
    try (InputStream input = new FileInputStream(file)) {
      props.load(input);
    } catch (IOException e) {
      throw new InternalError("Error, properties file not found");
    }
  }

  /**
   * Get a property from a properties file.
   * 
   * @param key the key of the property.
   * @return the property asked.
   */
  public static String getProperty(String key) {
    return props.getProperty(key);
  }

  /**
   * Get an integer property from a properties file.
   * 
   * @param key the key of the property.
   * @return the property asked.
   */
  public static int getIntProperty(String key) {
    return Integer.parseInt(props.getProperty(key));
  }
}
