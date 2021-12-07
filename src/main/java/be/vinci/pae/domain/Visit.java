package be.vinci.pae.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = VisitImpl.class)
public interface Visit extends VisitDTO {

  boolean checkDEM();

}
