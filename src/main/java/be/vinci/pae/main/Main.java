package be.vinci.pae.main;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import be.vinci.pae.utils.Config;
import be.vinci.pae.utils.SchedulerImpl;

/**
 * Main class.
 *
 */
public class Main {

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   * 
   * @return Grizzly HTTP server.
   */
  public static HttpServer startServer() {
    // Create a resource config that scans for JAX-RS resources and providers
    final ResourceConfig rc = new ResourceConfig()
        .packages("be.vinci.pae.api", "be.vinci.pae.utils", "be.vinci.pae.filters",
            "org.glassfish.jersey.examples.multipart")
        .register(JacksonFeature.class).register(MultiPartFeature.class)
        .property("jersey.config.server.wadl.disableWadl", true);

    // Create and start a new instance of grizzly http server
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(Config.getProperty("localURI")),
        rc);
  }

  /**
   * Main method.
   */
  public static void main(String[] args) throws IOException {
    // Load properties file
    if (args.length > 0) {
      Config.load(args[0]);
    } else {
      Config.load("prod.properties");
    }
    // Start the server
    final HttpServer server = startServer();
    System.out.println("Jersey app started at " + Config.getProperty("localURI"));
    SchedulerImpl.setSchedulerToVerifyOptions(24, TimeUnit.HOURS);
    // Listen to key press and shutdown server
    System.out.println("Hit enter to stop it...");
    System.in.read();
    server.shutdownNow();
  }

}
