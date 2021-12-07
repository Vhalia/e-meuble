package be.vinci.pae.uc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import be.vinci.pae.dataservices.UserDAO;
import be.vinci.pae.domain.Address;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.utils.Config;
import be.vinci.utils.ApplicationBinderForMocks;
import jakarta.ws.rs.NotFoundException;


class UserUCCImplTest {
  private static String correctUsername;
  private static String correctPassword;
  private static String correctPasswordHashed;

  private static UserUCC userUCC;
  private static UserDAO userDAO;
  private static DomainFactory domainFactory;
  private static UserDTO corrUser;
  private static UserDTO corrUserNotValidated;
  private static FurnitureDTO furnitureInOption;
  private static FurnitureDTO furniturePropo;
  private static Option optionUser1;
  private static UserDTO fakeClient;
  private static Address addressToGet;
  private static UserDTO userTest;
  private static FurnitureDTO soldFurniture;
  private static FurnitureDTO boughtFurniture;

  @BeforeAll
  static void initAll() {
    Config.load("prod.properties");
    correctUsername = "LightDid";
    correctPassword = "mot6";
    correctPasswordHashed = "$2a$09$V1GGIQZ2p/HSNW/7WO/oyOQ.SPJLFV0edGPm7tgshA8dJ59WLhQWe";
    ServiceLocator locator =
        ServiceLocatorUtilities.bind("locatorUserTest", new ApplicationBinderForMocks());
    userUCC = locator.getService(UserUCC.class);
    userDAO = locator.getService(UserDAO.class);
    domainFactory = locator.getService(DomainFactory.class);
    // userTest = locator.getService(User.class);

    corrUser = domainFactory.getUser();
    corrUserNotValidated = domainFactory.getUser();
    furnitureInOption = domainFactory.getFurniture();
    furniturePropo = domainFactory.getFurniture();
    optionUser1 = domainFactory.getOption();

    userTest = domainFactory.getUser();
    soldFurniture = domainFactory.getFurniture();
    boughtFurniture = domainFactory.getFurniture();
  }

  @BeforeEach
  void setup() throws DALErrorException {
    Mockito.reset(userDAO);

    corrUser.setId(1);
    corrUser.setUserName(correctUsername);
    corrUser.setPassword(correctPasswordHashed);
    corrUser.setRegistrationValidated(true);
    corrUser.setRole("CLI");
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);

    corrUserNotValidated.setUserName("userTest");
    corrUserNotValidated.setPassword(correctPasswordHashed);
    corrUserNotValidated.setRegistrationValidated(false);
    corrUserNotValidated.setRole("ADM");
    Mockito.when(userDAO.getUser("userTest")).thenReturn(corrUserNotValidated);

    furnitureInOption.setId(1);
    furnitureInOption.setState("ENOPT");

    optionUser1.setId(1);
    optionUser1.setIsCancel(false);
    optionUser1.setUserID(1);
    optionUser1.setFurnitureId(1);

    furniturePropo.setId(2);
    furnitureInOption.setState("PROPO");

    fakeClient = domainFactory.getUser();
    fakeClient.setName("fakename");
    fakeClient.setUserName("fakeusername");
    fakeClient.setSurname("fakesurname");
    fakeClient.setEmail("fakeemail@gmail.com");
    fakeClient.setFakeClient(true);

    addressToGet = domainFactory.getAddress();
    addressToGet.setId(1);
    addressToGet.setStreet("rue");
    addressToGet.setNbr("1");
    addressToGet.setBox("box");
    addressToGet.setPostalCode("4444");
    addressToGet.setCommune("commune");
    addressToGet.setCountry("pays");


    userTest.setId(8);
    userTest.setUserName("user");
    userTest.setPassword("password");
    userTest.setRegistrationValidated(true);
    userTest.setRole("CLI");

    soldFurniture.setId(9);
    soldFurniture.setSeller(userTest);

    boughtFurniture.setId(10);
  }


  @Test
  public void testDAO() {
    assertNotNull(userDAO);
  }

  @Test
  public void testFactory() {
    assertNotNull(domainFactory);
  }

  // Login

  @Test
  @DisplayName("login: testAllCorrect")
  public void loginTtestAllCorrect() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    UserDTO u = userUCC.login(correctUsername, correctPassword);
    // Mockito.when(userTest.checkPassword(correctPassword)).thenReturn(true);
    assertAll(() -> assertEquals(corrUser.getUserName(), u.getUserName()),
        () -> assertEquals(corrUser.getPassword(), u.getPassword()));
    // () -> Mockito.verify(userTest).checkPassword(correctPassword)

  }

  @Test
  @DisplayName("login: testWrongPassword")
  public void loginTestWrongPassword() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    assertThrows(UnauthorizedException.class, () -> userUCC.login(correctUsername, "0000"));
  }

  @Test
  @DisplayName("login: testWrongUsername")
  public void loginTestWrongUsername() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login("greyDid", correctPassword));
  }

  @Test
  @DisplayName("login: testAllWrong")
  public void loginTestAllWrong() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login("greyDid", "0000"));
  }

  @Test
  @DisplayName("login: testPasswordNull")
  public void loginTestPasswordNull() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    assertThrows(UnauthorizedException.class, () -> userUCC.login(correctUsername, null));
  }

  @Test
  @DisplayName("login: testUserNameNull")
  public void loginTestUserNameNull() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login(null, correctPassword));
  }

  @Test
  @DisplayName("login: testAllNull")
  public void loginTestAllNull() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login(null, null));
  }

  @Test
  @DisplayName("login: testAllEmpty")
  public void loginTestAllEmpty() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login("", ""));
  }

  @Test
  @DisplayName("login: testEmptyPassword")
  public void loginTestEmptyPassword() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    assertThrows(UnauthorizedException.class, () -> userUCC.login(correctUsername, ""));
  }

  @Test
  @DisplayName("login: testEmptyUserName")
  public void loginTestEmptyUserName() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(null);
    assertThrows(UnauthorizedException.class, () -> userUCC.login("", correctPassword));
  }

  // validateRegister

  @Test
  @DisplayName("validateRegister : testAllCorrect - request ADM")
  public void validateRegisterTestAllCorrectRequestADM() {
    UserDTO returned = userUCC.validateRegister(corrUserNotValidated, "ADM");
    assertAll(() -> assertEquals("ADM", returned.getRole()),
        () -> assertTrue(returned.isRegistrationValidated()));
  }

  @Test
  @DisplayName("validateRegister : testAllCorrect - request ANT")
  public void validateRegisterTestAllCorrectRequestANT() throws DALErrorException {
    corrUserNotValidated.setRole("ANT");
    UserDTO returned = userUCC.validateRegister(corrUserNotValidated, "ANT");
    Mockito.when(userDAO.getUser(corrUserNotValidated.getUserName()))
        .thenReturn(corrUserNotValidated);
    assertAll(() -> assertEquals("ANT", returned.getRole()),
        () -> assertTrue(returned.isRegistrationValidated()));
  }

  @Test
  @DisplayName("validateRegister : testWrongRole")
  public void validateRegisterTestWrongRole() {
    assertThrows(UnauthorizedException.class, () -> userUCC.validateRegister(corrUser, "FFF"));
  }

  @Test
  @DisplayName("validateRegister : testUserNotFound")
  public void validateRegisterTestUserNotFound() {
    corrUser.setUserName("test");
    assertThrows(NotFoundException.class, () -> userUCC.validateRegister(corrUser, "ADM"));
  }

  @Test
  @DisplayName("validateRegister : testRegistrationAlreadyValidated")
  public void validateRegisterTestRegistrationAlreadyValidated() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    assertThrows(UnauthorizedException.class, () -> userUCC.validateRegister(corrUser, "ADM"));
  }

  // getUsersNotValidated

  @Test
  @DisplayName("getUsersNotValidated : testListAllUserNotValidated")
  public void getUsersNotValidatedTestListAllUserNotValidated() throws DALErrorException {
    List<UserDTO> users = new ArrayList<UserDTO>();
    users.add(corrUserNotValidated);
    users.add(corrUserNotValidated);
    Mockito.when(userDAO.getUsersNotValidated()).thenReturn(users);
    assertEquals(users, userUCC.getUsersNotValidated());
  }

  @Test
  @DisplayName("getUsersNotValidated : testListAllUsersValidated")
  public void getUsersNotValidatedTestListAllUsersValidated() throws DALErrorException {
    List<UserDTO> users = new ArrayList<UserDTO>();
    users.add(corrUser);
    Mockito.when(userDAO.getUsersNotValidated()).thenReturn(users);
    assertEquals(users, userUCC.getUsersNotValidated());
  }

  @Test
  @DisplayName("getUsersNotValidated : testListEmpty")
  public void getUsersNotValidatedTestListEmpty() throws DALErrorException {
    List<UserDTO> users = new ArrayList<UserDTO>();
    Mockito.when(userDAO.getUsersNotValidated()).thenReturn(users);
    assertEquals(users, userUCC.getUsersNotValidated());
  }

  // refuseRegister

  @Test
  @DisplayName("refuseRegister : testAllCorrect")
  public void refuseRegisterTestAllCorrect() {
    UserDTO returned = userUCC.refuseRegister(corrUserNotValidated);
    assertAll(() -> assertEquals("CLI", returned.getRole()),
        () -> assertTrue(returned.isRegistrationValidated()));
  }

  @Test
  @DisplayName("refuseRegister : userNotFound")
  public void refuseRegisterUserNotFound() throws DALErrorException {
    Mockito.when(userDAO.getUser("pastrouve")).thenReturn(null);
    corrUserNotValidated.setUserName("pastrouve");
    assertThrows(NotFoundException.class, () -> userUCC.refuseRegister(corrUserNotValidated));
  }

  @Test
  @DisplayName("refuseRegister : alreadyValidated")
  public void refuseRegisterAlreadyValidated() throws DALErrorException {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    assertThrows(UnauthorizedException.class, () -> userUCC.refuseRegister(corrUser));
  }

  // getFurnituresWithAnOption

  @Test
  @DisplayName("getFurnituresWithAnOption : testNotEmptyList")
  public void getFurnituresWithAnOptionTestNotEmptyList() throws DALErrorException {
    List<FurnitureDTO> furnituresOption = new ArrayList<FurnitureDTO>();
    furnituresOption.add(furnitureInOption);
    Mockito.when(userDAO.getFurnituresWithAnOption(corrUser.getId())).thenReturn(furnituresOption);
    List<FurnitureDTO> result = userUCC.getFurnituresWithAnOption(1);
    assertEquals(furnituresOption, result);
  }

  @Test
  @DisplayName("getFurnituresWithAnOption : testEmptyList")
  public void getFurnituresWithAnOptionTestEmptyList() throws DALErrorException {
    List<FurnitureDTO> furnituresOption = new ArrayList<FurnitureDTO>();
    Mockito.when(userDAO.getFurnituresWithAnOption(corrUser.getId())).thenReturn(furnituresOption);
    List<FurnitureDTO> result = userUCC.getFurnituresWithAnOption(1);
    assertEquals(furnituresOption, result);
  }

  // research of furnitures

  @Test
  @DisplayName("getUsersByResearch : testAllCorrect")
  public void getUsersByResearchTestAllCorrect() {

    String testWord = "monMotClé";

    Mockito.when(userDAO.getUsersByResearch(testWord)).thenReturn(new ArrayList<UserDTO>());

    assertEquals(new ArrayList<FurnitureDTO>(), userUCC.getUsersByResearch(testWord));
  }

  @Test
  @DisplayName("getUsersByResearch : testNoWord")
  public void getUsersByResearchTestNoWord() {

    String testWord = null;

    Mockito.when(userDAO.getUsersByResearch(testWord)).thenReturn(new ArrayList<UserDTO>());

    assertThrows(IllegalArgumentException.class, () -> userUCC.getUsersByResearch(testWord));
  }

  // get tags

  @Test
  @DisplayName("getTags : testAllCorrect")
  public void getTagsTestAllCorrect() {
    Mockito.when(userDAO.getTags()).thenReturn(new ArrayList<String>());
    assertEquals(new ArrayList<String>(), userUCC.getTags());
  }

  // getUser with username

  @Test
  @DisplayName("getUser : test correct username")
  public void getUserTestCorrectUsername() {
    Mockito.when(userDAO.getUser(correctUsername)).thenReturn(corrUser);
    UserDTO userReturned = userUCC.getUser(correctUsername);
    assertEquals(corrUser, userReturned);
  }

  @Test
  @DisplayName("getUser : test username null or empty")
  public void getUserTestUsernameNullOrEmpty() {
    assertAll(() -> assertThrows(IllegalArgumentException.class, () -> userUCC.getUser("")),
        () -> assertThrows(IllegalArgumentException.class, () -> userUCC.getUser(null)));
  }

  @Test
  @DisplayName("getUser : test no user with this username")
  public void getUserTestNoUserWithThisUsername() {
    Mockito.when(userDAO.getUser("test")).thenReturn(null);
    assertThrows(NotFoundException.class, () -> userUCC.getUser("test"));
  }

  // create fake client

  @Test
  @DisplayName("createFakeClient : test all correct")
  public void createFakeClientTestAllCorrect() {
    UserDTO fakeClientReturned =
        userUCC.createFakeClient("fakename", "fakesurname", "fakeusername", "fakeemail@gmail.com");
    assertAll(() -> assertTrue(fakeClientReturned.isFakeClient()),
        () -> assertEquals(fakeClient.getName(), fakeClientReturned.getName()),
        () -> assertEquals(fakeClient.getSurname(), fakeClientReturned.getSurname()),
        () -> assertEquals(fakeClient.getUserName(), fakeClientReturned.getUserName()),
        () -> assertEquals(fakeClient.getEmail(), fakeClientReturned.getEmail()),
        () -> assertEquals("CLI", fakeClientReturned.getRole()));
  }

  @Test
  @DisplayName("createFakeClient : test incorrect parameter")
  public void createFakeClientTestIncorrectParameter() {
    assertAll(
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("", "fakesurname", "fakeusername",
                "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient(null, "fakesurname", "fakeusername",
                "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", "", "fakeusername", "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", null, "fakeusername",
                "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", "fakesurname", "", "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", "fakesurname", null, "fakeemail@gmail.com")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", "fakesurname", "fakeusername", "")),
        () -> assertThrows(IllegalArgumentException.class,
            () -> userUCC.createFakeClient("fakename", "fakesurname", "fakeusername", null)));
  }

  @Test
  @DisplayName("createFakeClient : test exception in dao handled")
  public void createFakeClientTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(userDAO).createUser(Mockito.any(UserDTO.class));
    assertThrows(SomethingWentWrongException.class, () -> userUCC.createFakeClient("fakename",
        "fakesurname", "fakeusername", "fakeemail@gmail.com"));
  }

  // get Address

  @Test
  @DisplayName("getAddress : test all correct")
  public void getAddressTestAllCorrect() {
    Mockito.when(userDAO.getAddressFromUserId(corrUser.getId())).thenReturn(addressToGet);
    Address adrReturned = userUCC.getAddress(corrUser);
    assertAll(() -> assertEquals(addressToGet.getId(), adrReturned.getId()),
        () -> assertEquals(addressToGet.getCountry(), adrReturned.getCountry()),
        () -> assertEquals(addressToGet.getStreet(), adrReturned.getStreet()),
        () -> assertEquals(addressToGet.getNbr(), adrReturned.getNbr()),
        () -> assertEquals(addressToGet.getBox(), adrReturned.getBox()),
        () -> assertEquals(addressToGet.getPostalCode(), adrReturned.getPostalCode()),
        () -> assertEquals(addressToGet.getCommune(), adrReturned.getCommune()));
  }

  @Test
  @DisplayName("getAddress : test user has no address")
  public void getAddressTestUserHasNoAddress() {
    Mockito.when(userDAO.getAddressFromUserId(corrUser.getId())).thenReturn(null);
    assertThrows(NotFoundException.class, () -> userUCC.getAddress(corrUser));
  }

  @Test
  @DisplayName("getAddress : test exception in dao handled")
  public void getAddressTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(userDAO).getAddressFromUserId(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class, () -> userUCC.getAddress(corrUser));
  }

  // add address

  @Test
  @DisplayName("addAddress : test address in parameter not correct")
  public void addAddressTestAddressInParameterNotCorrect() {
    Address adr = domainFactory.getAddress();
    assertAll(() -> assertThrows(IllegalArgumentException.class, () -> userUCC.addAddress(adr)),
        () -> assertThrows(IllegalArgumentException.class, () -> userUCC.addAddress(null)));
  }

  @Test
  @DisplayName("addAddress : test exception in dao handled")
  public void addAddressTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(userDAO)
        .createAddress(Mockito.any(Address.class));
    assertThrows(SomethingWentWrongException.class, () -> userUCC.addAddress(addressToGet));
  }

  // get User with id

  @Test
  @DisplayName("getUser : test all correct")
  public void getUserTestAllCorrect() {
    Mockito.when(userDAO.getUser(corrUser.getId())).thenReturn(corrUser);
    UserDTO userReturned = userUCC.getUser(corrUser.getId());
    assertEquals(corrUser, userReturned);
  }

  @Test
  @DisplayName("getUser : test incorrect id")
  public void getUserTestIncorrectId() {
    Mockito.when(userDAO.getUser(-1)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> userUCC.getUser(-1));
  }

  @Test
  @DisplayName("getUser : test exception in dao handled")
  public void getUserTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(userDAO).getUser(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class, () -> userUCC.getUser(Mockito.anyInt()));
  }

  // get Address with id

  @Test
  @DisplayName("getAddress by id : test all correct")
  public void getAddressByIdTestAllCorrect() {
    Mockito.when(userDAO.getAddress(addressToGet.getId())).thenReturn(addressToGet);
    Address adrReturned = userUCC.getAddress(addressToGet.getId());
    assertEquals(addressToGet, adrReturned);
  }

  @Test
  @DisplayName("getAddress by id : test incorrect id")
  public void getAddressByIdTestIncorrectId() {
    Mockito.when(userDAO.getAddress(-1)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> userUCC.getAddress(-1));
  }

  @Test
  @DisplayName("getAddress by id : test exception in dao handled")
  public void getAddressByIdTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(userDAO).getAddress(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class, () -> userUCC.getAddress(Mockito.anyInt()));
  }
  // test getSoldFurnitures

  @Test
  @DisplayName("getSoldFurnitures : test all correct")
  public void getSoldFurnituresTestAllCorrect() {
    List<FurnitureDTO> soldFurnitures = new ArrayList<FurnitureDTO>();
    soldFurnitures.add(soldFurniture);
    Mockito.when(userDAO.getSoldFurnitures(userTest.getId())).thenReturn(soldFurnitures);
    assertEquals(userUCC.getSoldFurnitures(userTest.getId()), soldFurnitures);
  }

  @Test
  @DisplayName("getSoldFurnitures : test wrong user id")
  public void getSoldFurnituresTestWrongUserId() {
    List<FurnitureDTO> soldFurnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(userDAO.getSoldFurnitures(userTest.getId())).thenReturn(soldFurnitures);
    assertTrue(userUCC.getSoldFurnitures(userTest.getId()).isEmpty());
  }

  // test getBoughtFurnitures

  @Test
  @DisplayName("getBoughtFurnitures : test all correct")
  public void getBoughtFurnituresTestAllCorrect() {
    List<FurnitureDTO> boughtFurnitures = new ArrayList<FurnitureDTO>();
    boughtFurnitures.add(boughtFurniture);
    Mockito.when(userDAO.getBoughtFurnitures(userTest.getId())).thenReturn(boughtFurnitures);
    assertEquals(userUCC.getBoughtFurnitures(userTest.getId()), boughtFurnitures);
  }

  @Test
  @DisplayName("getBoughtFurnitures : test wrong user id")
  public void getBoughtFurnituresTestWrongUserId() {
    List<FurnitureDTO> boughtFurnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(userDAO.getBoughtFurnitures(userTest.getId())).thenReturn(boughtFurnitures);
    assertTrue(userUCC.getBoughtFurnitures(userTest.getId()).isEmpty());
  }

}
