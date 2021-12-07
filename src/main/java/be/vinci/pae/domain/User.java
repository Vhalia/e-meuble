package be.vinci.pae.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface for the type User.
 */
@JsonDeserialize(as = UserImpl.class)
public interface User extends UserDTO {
  String[] ROLES = {"ANT", "CLI", "ADM"}; // antiquaire - client -
  // administrateur

  boolean checkPassword(String password);

  void cryptPassword();

  /**
   * Check if the role exist.
   * 
   * @return true if it's valid false otherwise.
   */
  static boolean checkRole(String role) {
    for (String r : ROLES) {
      if (r.equals(role)) {
        return true;
      }
    }
    return false;
  }

  boolean checkEmail();

  boolean checkIsCorrectUser();

}
