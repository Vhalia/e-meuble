package be.vinci.pae.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = FurnitureImpl.class)
public interface Furniture extends FurnitureDTO {

  String[] STATES = {"PROPO", "PASCO", "ENRES", "ENMAG", "ENVEN", "ENOPT", "RESER", "VENDU",
      "LIVRE", "EMPOR", "RETIR"};

  boolean checkPropo();

  boolean checkEnRes();

  boolean checkEnVen();

  boolean checkEnOpt();

  boolean checkNextState(String nextState);

  boolean checkCanHaveSpecialPrice();

  boolean checkIsCorrectFurniture();
}
