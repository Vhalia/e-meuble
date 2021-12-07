package be.vinci.pae.utils;

public class VariableChecker {

  /**
   * check if the price given in parameter is positive.
   * 
   * @param price the price to check.
   * @return true if the price is positive.
   */
  public static boolean checkPositive(double price) {
    return price >= 0;
  }

  /**
   * check if a string is empty or null.
   * 
   * @param s the string to verify.
   * @return true if s is not empty or null.
   */
  public static boolean checkStringNotEmpty(String s) {
    return s != null && !s.equals("");
  }

}
