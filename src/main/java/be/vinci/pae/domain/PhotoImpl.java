package be.vinci.pae.domain;

public class PhotoImpl implements Photo {

  private int id;

  private byte[] bytes;

  private boolean isScrollable;

  private String path;

  private String extension;

  /**
   * getter for the id.
   *
   * @return the id.
   */
  public int getId() {
    return id;
  }

  /**
   * setter for the id.
   *
   * @param id the new id.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * getter for the extension.
   * 
   * @return the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * setter for the extension.
   * 
   * @param extension the new extension.
   */
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * getter for the bytes.
   * 
   * @return the bytes.
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * setter for the bytes.
   * 
   * @param bytes the new bytes.
   */
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * getter for the isScrollable.
   * 
   * @return the isScrollable.
   */
  public boolean isScrollable() {
    return isScrollable;
  }

  /**
   * setter for the isScrollable.
   * 
   * @param isScrollable the new isScrollable.
   */
  public void setScrollable(boolean isScrollable) {
    this.isScrollable = isScrollable;
  }

  /**
   * getter for the path.
   * 
   * @return the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * setter for the path.
   * 
   * @param path the new path.
   */
  public void setPath(String path) {
    this.path = path;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((extension == null) ? 0 : extension.hashCode());
    result = prime * result + id;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PhotoImpl other = (PhotoImpl) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }



}
