package be.vinci.pae.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

  /**
   * Allows to create a new file from an InputStream.
   * 
   * @param file the file to upload.
   */
  public static void createFile(InputStream file, String path) throws IOException {
    OutputStream outStream = null;
    try {
      byte[] buffer = file.readAllBytes();
      File targetFile = new File(path);
      targetFile.createNewFile();
      outStream = new FileOutputStream(targetFile);
      outStream.write(buffer);
    } finally {
      if (outStream != null) {
        outStream.close();
      }
      file.close();
    }
  }

  /**
   * Allows to read a file and return an array of byte from a path.
   * 
   * @param path the path where is the file to read.
   * @return buffer an array of bytes containing the bytes of the file.
   */
  public static byte[] readFile(String path) throws IOException {
    byte[] buffer = null;
    InputStream file = null;
    try {
      file = new FileInputStream(new File(path));
      buffer = file.readAllBytes();
    } finally {
      file.close();
    }
    return buffer;
  }

}
