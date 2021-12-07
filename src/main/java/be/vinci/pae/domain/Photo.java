package be.vinci.pae.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = PhotoImpl.class)
public interface Photo {

  byte[] getBytes();

  void setBytes(byte[] bytes);

  boolean isScrollable();

  void setScrollable(boolean isScrollable);

  String getPath();

  void setPath(String path);

  String getExtension();

  void setExtension(String type);

  int getId();

  void setId(int id);

}
