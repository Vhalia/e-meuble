package be.vinci.pae.uc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import be.vinci.pae.dataservices.VisitDAO;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.WrongStateException;
import be.vinci.pae.utils.Config;
import be.vinci.utils.ApplicationBinderForVisitTests;
import jakarta.ws.rs.NotFoundException;

class VisitUCCImplTest {

  private static VisitUCC visitUCC;

  // mocks
  private static UserUCC mockUserUCC;
  private static VisitDAO mockVisitDAO;
  private static FurnitureUCC mockFurnitureUCC;

  // factory
  private static DomainFactory domainFactory;

  // Dto
  private static UserDTO fakeClient;
  private static UserDTO userWhoRequested;
  private static Address storageAddress;
  private static Address addressOfuserWhoRequested;
  private static VisitDTO visitRequest;
  private static List<FurnitureDTO> furnitures;
  private static VisitDTO visitRequestAfterInsertion;

  // files
  private static List<InputStream> files;

  @BeforeAll
  static void init() throws Exception {
    Config.load("prod.properties");
    ServiceLocator locator =
        ServiceLocatorUtilities.bind("locatorVisitTest", new ApplicationBinderForVisitTests());
    visitUCC = locator.getService(VisitUCC.class);
    mockUserUCC = locator.getService(UserUCC.class);
    mockVisitDAO = locator.getService(VisitDAO.class);
    domainFactory = locator.getService(DomainFactory.class);
    mockFurnitureUCC = locator.getService(FurnitureUCC.class);

    // Create dto
    // fakeClient
    fakeClient = domainFactory.getUser();
    fakeClient.setId(1);
    fakeClient.setName("name");
    fakeClient.setSurname("surname");
    fakeClient.setUserName(fakeClient.getName() + "-" + fakeClient.getSurname());
    fakeClient.setEmail("email@gmail.com");
    fakeClient.setRole("CLI");
    fakeClient.setRegistrationDate(Date.valueOf(LocalDate.now()));
    // visit
    visitRequest = domainFactory.getVisit();
    // client who requested
    userWhoRequested = domainFactory.getUser();
    // address of client who requested
    addressOfuserWhoRequested = domainFactory.getAddress();
    addressOfuserWhoRequested.setCommune("commune");
    addressOfuserWhoRequested.setStreet("rue");
    addressOfuserWhoRequested.setPostalCode("code postal");
    addressOfuserWhoRequested.setCountry("pays");
    addressOfuserWhoRequested.setNbr("1");
    userWhoRequested.setAddress(addressOfuserWhoRequested);
    // Storage address
    storageAddress = domainFactory.getAddress();
    storageAddress.setCommune("commune");
    storageAddress.setStreet("rue");
    storageAddress.setPostalCode("code postal");
    storageAddress.setCountry("pays");
    storageAddress.setNbr("1");
    // files
    files = new ArrayList<InputStream>();
    // furnitures of the visit
    furnitures = new ArrayList<FurnitureDTO>();
    FurnitureDTO f1 = domainFactory.getFurniture();
    f1.setType("Bureau");
    f1.setDescription("Description1");
    createListOfFilesForAFurniture(3, f1, files);
    furnitures.add(f1);
    FurnitureDTO f2 = domainFactory.getFurniture();
    f2.setType("Chaise");
    f2.setDescription("Description2");
    createListOfFilesForAFurniture(2, f2, files);
    furnitures.add(f2);
  }

  @BeforeEach
  void setUp() throws Exception {
    visitRequest.setStorageAddress(storageAddress);
    visitRequest.setFurnitures(furnitures);
    visitRequest.setClient(userWhoRequested);
    visitRequest.setCancellationNote(null);
    visitRequest.setUsersTimeSlot("time slot");
    userWhoRequested.setName("name");
    userWhoRequested.setSurname("surname");
    userWhoRequested.setEmail("email@gmail.com");
    userWhoRequested.setRole("CLI");
    userWhoRequested.setRegistrationDate(Date.valueOf(LocalDate.now()));

    Mockito.reset(mockVisitDAO, mockUserUCC, mockFurnitureUCC);
    String pseudo =
        visitRequest.getClient().getName() + "-" + visitRequest.getClient().getSurname();
    Mockito
        .when(mockUserUCC.createFakeClient(visitRequest.getClient().getName(),
            visitRequest.getClient().getSurname(), pseudo, visitRequest.getClient().getEmail()))
        .thenReturn(fakeClient);

    Mockito.when(mockUserUCC.getAddress(userWhoRequested)).thenReturn(addressOfuserWhoRequested);

    visitRequestAfterInsertion = visitRequest;
    visitRequestAfterInsertion.setId(1);
    Mockito.when(mockVisitDAO.insertVisitRequest(visitRequest))
        .thenReturn(visitRequestAfterInsertion);

    for (int i = 0; i < visitRequest.getFurnitures().size(); i++) {
      FurnitureDTO furnitureBeforeInsertion = visitRequest.getFurnitures().get(i);
      FurnitureDTO furnitureAfterInsertion = furnitureBeforeInsertion;
      furnitureAfterInsertion.setState("PROPO");
      furnitureAfterInsertion.setSeller(userWhoRequested);
      furnitureAfterInsertion.setId(i + 1);
      Mockito.when(mockFurnitureUCC.createFurniture(Mockito.eq(furnitureBeforeInsertion),
          Mockito.eq(visitRequest), Mockito.anyList())).thenReturn(furnitureAfterInsertion);
    }

  }

  @AfterEach
  public void clean() {
    if (visitRequest.getFurnitures() != null) {
      for (int i = 0; i < visitRequest.getFurnitures().size(); i++) {
        for (int j = 0; j < visitRequest.getFurnitures().get(i).getPhotos().size(); j++) {
          try {
            Files.delete(new File(visitRequest.getFurnitures().get(i).getPhotos().get(j).getPath())
                .toPath());
          } catch (IOException e) {
            // System.out.println(e);
          }

        }
      }
    }
  }

  @AfterAll
  public static void cleanAll() throws IOException {
    for (int i = 0; i < files.size(); i++) {
      files.get(i).close();
    }
  }

  private static FurnitureDTO createListOfFilesForAFurniture(int n, FurnitureDTO f,
      List<InputStream> files) throws IOException {
    List<Photo> photos = new ArrayList<Photo>();
    for (int i = 0; i < n; i++) {
      String path = "images/imageForTests" + i + ".png";
      Photo photo = domainFactory.getPhoto();
      photo.setPath(path);
      File file = new File(path);
      file.createNewFile();
      files.add(new FileInputStream(file));
      photos.add(photo);
    }
    f.setPhotos(photos);
    return f;
  }

  // createVisitRequest

  @Test
  @DisplayName("createVisitRequest : Test correct visit with fake client and storage address set")
  void createVisitRequestTestCorrectVisitWithFakeClientAndStorageAddressSet() {
    VisitDTO visitReturned = visitUCC.createVisitRequest(visitRequest, files, true);
    assertAll(() -> assertEquals(visitRequestAfterInsertion.getId(), visitReturned.getId()),
        () -> assertEquals("DEM", visitReturned.getState()),
        () -> Mockito.verify(mockUserUCC).createFakeClient(userWhoRequested.getName(),
            userWhoRequested.getSurname(),
            userWhoRequested.getName() + "-" + userWhoRequested.getSurname(),
            userWhoRequested.getEmail()),
        () -> Mockito.verify(mockUserUCC, Mockito.never()).getAddress(fakeClient),
        () -> Mockito.verify(mockUserUCC).addAddress(Mockito.any(Address.class)),
        () -> assertEquals(storageAddress, visitReturned.getStorageAddress()),
        () -> Mockito.verify(mockFurnitureUCC, Mockito.times(visitRequest.getFurnitures().size()))
            .createFurniture(Mockito.any(FurnitureDTO.class), Mockito.eq(visitRequest),
                Mockito.anyList()));
  }

  @Test
  @DisplayName("createVisitRequest : Test correct visit with "
      + " exisiting client and storage address set")
  void createVisitRequestTestCorrectVisitWithExisitingClientAndStorageAddressSet() {
    userWhoRequested.setUserName("pseudo");
    VisitDTO visitReturned = visitUCC.createVisitRequest(visitRequest, files, false);
    assertAll(() -> assertEquals(visitRequestAfterInsertion.getId(), visitReturned.getId()),
        () -> assertEquals("DEM", visitReturned.getState()),
        () -> Mockito.verify(mockUserUCC, Mockito.never()).createFakeClient(
            userWhoRequested.getName(), userWhoRequested.getSurname(),
            userWhoRequested.getName() + "-" + userWhoRequested.getSurname(),
            userWhoRequested.getEmail()),
        () -> Mockito.verify(mockUserUCC, Mockito.never()).getAddress(userWhoRequested),
        () -> Mockito.verify(mockUserUCC).addAddress(Mockito.any(Address.class)),
        () -> assertEquals(storageAddress, visitReturned.getStorageAddress()),
        () -> Mockito.verify(mockFurnitureUCC, Mockito.times(visitRequest.getFurnitures().size()))
            .createFurniture(Mockito.any(FurnitureDTO.class), Mockito.eq(visitRequest),
                Mockito.anyList()));
  }

  @Test
  @DisplayName("createVisitRequest : Test correct visit with exisiting "
      + "client and use his address to store")
  void createVisitRequestTestCorrectVisitWithExisitingClientAndUseHisAddressToStore() {
    userWhoRequested.setUserName("pseudo");
    visitRequest.setStorageAddress(null);
    VisitDTO visitReturned = visitUCC.createVisitRequest(visitRequest, files, false);
    assertAll(() -> assertEquals(visitRequestAfterInsertion.getId(), visitReturned.getId()),
        () -> assertEquals("DEM", visitReturned.getState()),
        () -> Mockito.verify(mockUserUCC, Mockito.never()).createFakeClient(
            userWhoRequested.getName(), userWhoRequested.getSurname(),
            userWhoRequested.getName() + "-" + userWhoRequested.getSurname(),
            userWhoRequested.getEmail()),
        () -> Mockito.verify(mockUserUCC).getAddress(userWhoRequested),
        () -> Mockito.verify(mockUserUCC, Mockito.never()).addAddress(Mockito.any(Address.class)),
        () -> assertEquals(userWhoRequested.getAddress(), visitReturned.getStorageAddress()),
        () -> Mockito.verify(mockFurnitureUCC, Mockito.times(visitRequest.getFurnitures().size()))
            .createFurniture(Mockito.any(FurnitureDTO.class), Mockito.eq(visitRequest),
                Mockito.anyList()));
  }

  @Test
  @DisplayName("createVisitRequest : Test existing cancellation note")
  void createVisitRequestTestExistingCancellationNote() {
    visitRequest.setCancellationNote("note d'annulation");
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
  }

  @Test
  @DisplayName("createVisitRequest : Test no client")
  void createVisitRequestTestNoClient() {
    visitRequest.setClient(null);
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
  }

  @Test
  @DisplayName("createVisitRequest : Test user's attributes are incorrect")
  void createVisitRequestTestUsersAttributesAreIncorrect() {
    userWhoRequested.setName("");
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
  }

  @Test
  @DisplayName("createVisitRequest : Test user's time slot is empty or null")
  void createVisitRequestTestUsersTimeSlotIsEmptyOrNull() {
    visitRequest.setUsersTimeSlot("");
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
    visitRequest.setUsersTimeSlot(null);
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
  }

  @Test
  @DisplayName("createVisitRequest : Test list of furnitures null or empty")
  void createVisitRequestTestListOfFurnituresNullOrEmpty() {
    visitRequest.setFurnitures(new ArrayList<FurnitureDTO>());
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
    visitRequest.setFurnitures(null);
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
  }

  @Test
  @DisplayName("createVisitRequest : Test a furniture is not correct")
  void createVisitRequestTestAFurnitureIsNotCorrect() {
    FurnitureDTO incorrectF = domainFactory.getFurniture();
    visitRequest.getFurnitures().add(incorrectF);
    assertThrows(IllegalArgumentException.class,
        () -> visitUCC.createVisitRequest(visitRequest, files, false));
    visitRequest.getFurnitures().remove(incorrectF);
  }

  // get all visits

  @Test
  @DisplayName("getAllVisits : Test all correct")
  void getAllVisitsTestAllCorrect() {
    List<VisitDTO> visits = new ArrayList<VisitDTO>();
    visits.add(visitRequest);
    Mockito.when(mockVisitDAO.getAllVisits()).thenReturn(visits);
    List<VisitDTO> visitsReturned = visitUCC.getAllVisits();
    assertEquals(visits, visitsReturned);
  }

  @Test
  @DisplayName("getAllVisits : Test empty list")
  void getAllVisitsTestEmptyList() {
    List<VisitDTO> visits = new ArrayList<VisitDTO>();
    Mockito.when(mockVisitDAO.getAllVisits()).thenReturn(visits);
    List<VisitDTO> visitsReturned = visitUCC.getAllVisits();
    assertEquals(visits, visitsReturned);
  }

  @Test
  @DisplayName("getAllVisits : test exception in dao handled")
  void getAllVisitsTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(mockVisitDAO).getAllVisits();
    assertThrows(SomethingWentWrongException.class, () -> visitUCC.getAllVisits());
  }

  // get visit

  @Test
  @DisplayName("getVisit : test all correct")
  void getVisitTestAllCorrect() {
    Mockito.when(mockVisitDAO.getVisit(visitRequest.getId())).thenReturn(visitRequest);
    Mockito.when(mockUserUCC.getUser(userWhoRequested.getId())).thenReturn(userWhoRequested);
    Mockito.when(mockUserUCC.getAddress(addressOfuserWhoRequested.getId()))
        .thenReturn(addressOfuserWhoRequested);
    Mockito.when(mockFurnitureUCC.getAllFurnituresOfAVisit(visitRequest.getId()))
        .thenReturn(furnitures);
    VisitDTO visitReturned = visitUCC.getVisit(visitRequest.getId());

    assertAll(() -> assertEquals(visitRequest.getId(), visitReturned.getId()),
        () -> assertEquals(visitRequest.getClient(), visitReturned.getClient()),
        () -> assertEquals(visitRequest.getStorageAddress(), visitReturned.getStorageAddress()),
        () -> assertEquals(visitRequest.getFurnitures(), visitReturned.getFurnitures()));
  }

  @Test
  @DisplayName("getVisit : test incorrect id")
  void getVisitTestIncorrectId() {
    Mockito.when(mockVisitDAO.getVisit(-1)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> visitUCC.getVisit(-1));
  }

  @Test
  @DisplayName("getVisit : test exception in dao handled")
  void getVisitTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(mockVisitDAO).getVisit(90);
    assertThrows(SomethingWentWrongException.class, () -> visitUCC.getVisit(90));
  }

  // test for cancelVisit
  @Test
  @DisplayName("cancelVisit : test all correct")
  void cancelVisitTestAllCorrect() {
    VisitDTO visitCancel = domainFactory.getVisit();
    visitCancel.setId(1);
    visitCancel.setState("ANN");
    visitCancel.setCancellationNote("trop grand");
    visitRequestAfterInsertion.setState("DEM");
    visitRequestAfterInsertion.setCancellationNote("trop grand");


    Mockito.when(mockVisitDAO.cancelAVisit(visitRequestAfterInsertion)).thenReturn(visitCancel);
    VisitDTO v = visitUCC.cancelAVisit(visitRequestAfterInsertion);

    assertAll(() -> assertNotNull(v.getCancellationNote()),
        () -> assertEquals("ANN", v.getState()));
  }

  @Test
  @DisplayName("cancelVisit: test incorrect state")
  void cancelVisitTestIncorrectState() {
    VisitDTO visitAlreadyCancel = domainFactory.getVisit();
    visitAlreadyCancel.setId(1);
    visitAlreadyCancel.setState("ANN");
    visitAlreadyCancel.setCancellationNote("trop grand");

    assertThrows(WrongStateException.class, () -> visitUCC.cancelAVisit(visitAlreadyCancel));
  }

  @Test
  @DisplayName("confirmVisitRequest : Test all correct")
  void confirmVisitRequestTestAllCorrect() {

    LocalDateTime time = LocalDateTime.now();

    VisitDTO visitDem = domainFactory.getVisit();
    visitDem.setId(1);
    visitDem.setVisitDateTime(time);
    visitDem.setState("DEM");

    VisitDTO visitConf = domainFactory.getVisit();
    visitConf.setId(1);
    visitConf.setVisitDateTime(time);
    visitConf.setState("CONF");

    VisitDTO visitReturned = visitUCC.confirmVisit(visitDem);

    assertAll(() -> assertEquals(visitConf.getId(), visitReturned.getId()),
        () -> assertEquals(visitConf.getVisitDateTime(), visitReturned.getVisitDateTime()),
        () -> assertEquals(visitConf.getState(), visitReturned.getState()));

  }

  @Test
  @DisplayName("confirmVisitRequest : wrong state conf")
  void confirmVisitRequestTestWrongStateConf() {

    LocalDateTime time = LocalDateTime.now();

    VisitDTO visitDem = domainFactory.getVisit();
    visitDem.setId(0);
    visitDem.setVisitDateTime(time);
    visitDem.setState("CONF");

    assertThrows(WrongStateException.class, () -> visitUCC.confirmVisit(visitDem));
  }

  @Test
  @DisplayName("confirmVisitRequest : wrong state ann")
  void confirmVisitRequestTestWrongStateAnn() {

    LocalDateTime time = LocalDateTime.now();

    VisitDTO visitDem = domainFactory.getVisit();
    visitDem.setId(0);
    visitDem.setVisitDateTime(time);
    visitDem.setState("ANN");

    assertThrows(WrongStateException.class, () -> visitUCC.confirmVisit(visitDem));
  }

  @Test
  @DisplayName("confirmVisitRequest : no date")
  void confirmVisitRequestTestNoDate() {

    VisitDTO visitDem = domainFactory.getVisit();
    visitDem.setId(0);
    visitDem.setState("DEM");

    assertThrows(IllegalArgumentException.class, () -> visitUCC.confirmVisit(visitDem));
  }

  @Test
  @DisplayName("getVisitsOfAUser : all good")
  void getVisitsOfAUserTestAllGood() {

    List<VisitDTO> l = new ArrayList<VisitDTO>();
    Mockito.when(mockVisitDAO.getVisitsOfAUser(10)).thenReturn(l);
    assertEquals(l, visitUCC.getVisitsOfAUser(10));

  }

}
