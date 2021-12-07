package be.vinci.pae.domain;

import java.sql.Date;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface for the type Address.
 */
@JsonDeserialize(as = OptionImpl.class)
public interface Option {

  int getId();

  void setId(int id);

  boolean getIsCancel();

  void setIsCancel(boolean isCancel);

  Date getLimitDate();

  void setLimitDate(Date limitDate);

  int getDuration();

  void setDuration(int duration);

  int getFurnitureId();

  void setFurnitureId(int furnitureId);

  int getUserId();

  void setUserID(int userId);

  int getDaysLeft();

  void setDaysLeft(int daysLeft);

}
