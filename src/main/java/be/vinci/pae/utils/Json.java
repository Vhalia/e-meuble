
package be.vinci.pae.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import be.vinci.pae.views.Views;

public class Json {

  private static final ObjectMapper jsonMapper = new ObjectMapper();


  /**
   * check viewable/sendable variables took from backend-java course.
   * 
   * @param <T> type of return
   * @param item the object to verify(its attributes)
   * @param targetClass the class Views which define public or Internal attributes
   */
  public static <T> T filterPublicJsonView(T item, Class<T> targetClass) {

    try {
      // serialize using JSON Views : public view (all fields not required in the
      // views are set to null)
      String publicItemAsString =
          jsonMapper.writerWithView(Views.Public.class).writeValueAsString(item);
      // deserialize using JSON Views : Public View
      return jsonMapper.readerWithView(Views.Public.class).forType(targetClass)
          .readValue(publicItemAsString);

    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }

  }

}
