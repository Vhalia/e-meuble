package be.vinci.pae.domain;

import java.sql.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import be.vinci.pae.views.Views;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionImpl implements Option {
  @JsonView(Views.Public.class)
  private int id;

  @JsonView(Views.Public.class)
  private boolean isCancel;

  @JsonView(Views.Public.class)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date limitDate;

  @JsonView(Views.Public.class)
  private int duration;

  @JsonView(Views.Public.class)
  private int furnitureId;

  @JsonView(Views.Public.class)
  private int userId;

  @JsonView(Views.Public.class)
  private int daysLeft;


  public int getId() {
    return this.id;
  }


  public void setId(int id) {
    this.id = id;
  }


  public boolean getIsCancel() {
    return this.isCancel;
  }


  public void setIsCancel(boolean isCancel) {
    this.isCancel = isCancel;

  }


  public Date getLimitDate() {
    return this.limitDate;
  }


  public void setLimitDate(Date limitDate) {
    this.limitDate = limitDate;
  }


  public int getDuration() {
    return this.duration;
  }


  public void setDuration(int duration) {
    this.duration = duration;
  }


  public int getFurnitureId() {
    return this.furnitureId;
  }


  public void setFurnitureId(int furnitureId) {
    this.furnitureId = furnitureId;
  }


  public int getUserId() {
    return this.userId;
  }


  public void setUserID(int userId) {
    this.userId = userId;
  }

  public int getDaysLeft() {
    return this.daysLeft;
  }


  public void setDaysLeft(int daysLeft) {
    this.daysLeft = daysLeft;
  }

}
