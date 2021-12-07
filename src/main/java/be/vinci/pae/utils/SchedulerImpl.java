package be.vinci.pae.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import be.vinci.pae.uc.FurnitureUCC;

public class SchedulerImpl {

  static FurnitureUCC furnitureUCC;

  static {
    ServiceLocator locator =
        ServiceLocatorUtilities.bind("locatorForScheduler", new ApplicationBinder());
    furnitureUCC = locator.getService(FurnitureUCC.class);
  }

  /**
   * Set a scheduler that will execute periodically the method verifyAllOptions.
   * 
   * @param time time between two executions.
   * @param unit unit of the time parameter.
   */
  public static void setSchedulerToVerifyOptions(int time, TimeUnit unit) {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(() -> furnitureUCC.verifyAllOptions(), 0, time, unit);
  }

}
