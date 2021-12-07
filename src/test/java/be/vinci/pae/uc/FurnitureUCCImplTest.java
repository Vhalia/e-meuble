package be.vinci.pae.uc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
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
import be.vinci.pae.dataservices.FurnitureDAO;
import be.vinci.pae.dataservices.UserDAO;
import be.vinci.pae.domain.DomainFactory;
import be.vinci.pae.domain.FurnitureDTO;
import be.vinci.pae.domain.Option;
import be.vinci.pae.domain.Photo;
import be.vinci.pae.domain.UserDTO;
import be.vinci.pae.domain.VisitDTO;
import be.vinci.pae.exceptions.AlreadyCancelException;
import be.vinci.pae.exceptions.DALErrorException;
import be.vinci.pae.exceptions.IncorrectDurationException;
import be.vinci.pae.exceptions.SomethingWentWrongException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.exceptions.WrongStateException;
import be.vinci.pae.utils.Config;
import be.vinci.utils.ApplicationBinderForMocks;
import jakarta.ws.rs.NotFoundException;


class FurnitureUCCImplTest {


  private static double correctPurchasePrice;
  private static int correctId;
  private static String correctState;
  private static Date correctDateCarryFromClient;
  private static double correctSellPrice;
  private static double correctSpecialPrice;
  private static String correctSellState;
  private static int correctSellId;

  private static UserDAO userDAO;
  private static FurnitureUCC furnitureUCC;
  private static UserUCC userUCC;
  private static FurnitureDAO furnitureDAO;
  private static DomainFactory domainFactory;
  private static FurnitureDTO corrFurniture;
  private static FurnitureDTO initialFurniture;
  private static FurnitureDTO corrSellFurniture;
  private static FurnitureDTO initialSellFurniture;
  private static FurnitureDTO initialFurnitureP;
  private static FurnitureDTO optionFurniture;
  private static FurnitureDTO removeOptionFurniture;
  private static Option option;
  private static Option removeOption;
  private static UserDTO corrUser;
  private static UserDTO userAdmin;

  private static Photo photo;
  private static Photo photoAfterInsertion;
  private static FurnitureDTO furnitureToInsertPhoto;
  private static InputStream file;

  private static FurnitureDTO furnitureToFavPhoto;
  private static Photo photoFav;

  private static FurnitureDTO furnitureToCreate;
  private static VisitDTO visit;
  private static List<InputStream> filesOfFurnitureToCreate;

  private static List<FurnitureDTO> furnituresOfAVisit;

  private static Photo corrPhoto;

  private static FurnitureDTO furnitureWithFavPhoto;

  @BeforeAll
  static void initAll() throws ParseException, IOException {
    Config.load("prod.properties");
    correctSellPrice = 200;
    correctSpecialPrice = 100;
    correctPurchasePrice = 140;
    correctId = 2;
    correctSellId = 1;
    correctState = "ENMAG";
    correctDateCarryFromClient = Date.valueOf("2021-03-28");

    correctSellState = "ENVEN";
    ServiceLocator locator =
        ServiceLocatorUtilities.bind("locatorFurnitureTest", new ApplicationBinderForMocks());
    furnitureUCC = locator.getService(FurnitureUCC.class);
    userUCC = locator.getService(UserUCC.class);
    furnitureDAO = locator.getService(FurnitureDAO.class);
    domainFactory = locator.getService(DomainFactory.class);
    userDAO = locator.getService(UserDAO.class);

    removeOption = domainFactory.getOption();
    option = domainFactory.getOption();
    optionFurniture = domainFactory.getFurniture();
    removeOptionFurniture = domainFactory.getFurniture();
    corrFurniture = domainFactory.getFurniture();
    corrFurniture.setId(correctId);
    corrFurniture.setState(correctState);
    corrUser = domainFactory.getUser();

    corrSellFurniture = domainFactory.getFurniture();

    photo = domainFactory.getPhoto();
    String path = "images/imageForTests.png";
    photo.setPath(path);
    File f = new File(path);
    f.createNewFile();
    file = new FileInputStream(f);

    photoAfterInsertion = domainFactory.getPhoto();
    photoAfterInsertion.setId(1);
    photoAfterInsertion.setPath(path);
    photoAfterInsertion.setScrollable(false);
    photoAfterInsertion.setExtension("png");

    furnitureToInsertPhoto = domainFactory.getFurniture();
    furnitureToInsertPhoto.setId(8);
    furnitureToInsertPhoto.setState("PROPO");

    corrPhoto = domainFactory.getPhoto();
    furnitureWithFavPhoto = domainFactory.getFurniture();

    userAdmin = domainFactory.getUser();
  }

  @BeforeEach
  private void setup() throws DALErrorException, IOException {

    Mockito.reset(userDAO, furnitureDAO);

    initialFurnitureP = domainFactory.getFurniture();
    initialFurnitureP.setId(correctId);
    initialFurnitureP.setState("PROPO");
    initialFurniture = domainFactory.getFurniture();
    initialFurniture.setId(correctId);
    initialFurniture.setState("PROPO");
    corrSellFurniture.setId(correctSellId);
    corrSellFurniture.setState(correctSellState);
    corrSellFurniture.setSellPrice(correctSellPrice);
    corrSellFurniture.setSpecialPrice(correctSpecialPrice);

    initialSellFurniture = domainFactory.getFurniture();
    initialSellFurniture.setId(correctSellId);
    initialSellFurniture.setState("ENMAG");

    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(initialFurniture);
    Mockito.when(furnitureDAO.getFurniture(correctSellId)).thenReturn(initialSellFurniture);

    optionFurniture.setState("ENVEN");
    optionFurniture.setId(1);
    option.setDuration(4);
    option.setId(1);
    option.setFurnitureId(optionFurniture.getId());
    option.setLimitDate(Date.valueOf(LocalDate.now().plusDays(2)));
    option.setUserID(corrUser.getId());

    corrUser.setId(1);
    corrUser.setUserName("test");
    corrUser.setPassword("mot6");
    corrUser.setRegistrationValidated(true);
    corrUser.setRole("CLI");

    removeOptionFurniture.setState("ENOPT");
    removeOptionFurniture.setId(3);
    removeOption.setDuration(5);
    removeOption.setDaysLeft(5);
    removeOption.setId(2);
    removeOption.setUserID(corrUser.getId());
    removeOption.setFurnitureId(removeOptionFurniture.getId());
    setOptionLimiteDate(removeOption);
    Mockito.when(furnitureDAO.getFurniture(removeOptionFurniture.getId()))
        .thenReturn(removeOptionFurniture);
    Mockito.when(furnitureDAO.getOption(removeOption)).thenReturn(removeOption);
    Mockito.when(userDAO.getUser(corrUser.getId())).thenReturn(corrUser);

    furnitureToFavPhoto = domainFactory.getFurniture();
    furnitureToFavPhoto.setId(9);
    List<Photo> photosOfFurniture = new ArrayList<Photo>();
    photoFav = domainFactory.getPhoto();
    photoFav.setId(2);
    photosOfFurniture.add(photoFav);
    furnitureToFavPhoto.setPhotos(photosOfFurniture);

    furnitureToCreate = domainFactory.getFurniture();
    furnitureToCreate.setDescription("desc");
    furnitureToCreate.setType("type");
    List<Photo> photosOfFurnitureToCreate = new ArrayList<Photo>();
    filesOfFurnitureToCreate = new ArrayList<InputStream>();
    for (int i = 0; i < 3; i++) {
      Photo p = domainFactory.getPhoto();
      p.setPath("images/imageForTestsCreateFurniture" + i);
      photosOfFurnitureToCreate.add(p);
      File f = new File(p.getPath());
      f.createNewFile();
      filesOfFurnitureToCreate.add(new FileInputStream(f));
    }
    furnitureToCreate.setPhotos(photosOfFurnitureToCreate);
    visit = domainFactory.getVisit();
    visit.setClient(corrUser);

    furnituresOfAVisit = new ArrayList<FurnitureDTO>();
    for (int i = 0; i < 3; i++) {
      FurnitureDTO f = domainFactory.getFurniture();
      f.setId(i + 1);
      f.setSeller(corrUser);
      furnituresOfAVisit.add(f);
    }
    corrPhoto.setId(8);
    corrPhoto.setScrollable(true);

    furnitureWithFavPhoto.setId(9);
    furnitureWithFavPhoto.setFavouritePhoto(corrPhoto);

    userAdmin.setId(3);
    userAdmin.setRole("ADM");

  }

  @AfterEach
  public void clean() {
    new File(photo.getPath()).delete();
    for (int i = 0; i < furnitureToCreate.getPhotos().size(); i++) {
      try {
        Files.delete(new File(furnitureToCreate.getPhotos().get(i).getPath()).toPath());
      } catch (IOException e) {
        // System.out.println(e);
      }

    }
  }

  @AfterAll
  public static void cleanAll() throws IOException {
    file.close();
  }


  private void setOptionLimiteDate(Option o) {
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, o.getDuration());
    Date date = new Date(c.getTimeInMillis());
    o.setLimitDate(date);
  }

  @Test
  public void testDAO() {
    assertNotNull(furnitureDAO);
  }

  @Test
  public void testFactory() {
    assertNotNull(domainFactory);
  }

  // fixPurchasePrice

  @Test
  @DisplayName("fixPurchasePrice : TestAllCorrect")
  public void fixPurchasePriceTestAllCorrect() {

    FurnitureDTO f = furnitureUCC.fixPurchasePrice(correctId, correctPurchasePrice, correctState,
        correctDateCarryFromClient);

    assertAll(() -> assertEquals(corrFurniture.getId(), f.getId()),
        () -> assertNotNull(f.getDateCarryToStore()), () -> assertEquals("ENMAG", f.getState()));
  }

  @Test
  @DisplayName("fixPurchasePrice : TestInexistingId")
  public void fixPurchasePriceTestInexistingId() {

    assertThrows(NotFoundException.class, () -> furnitureUCC.fixPurchasePrice(0,
        correctPurchasePrice, correctState, correctDateCarryFromClient));
  }

  @Test
  @DisplayName("fixPurchasePrice : TestNegativePrice")
  public void fixPurchasePriceTestNegativePrice() {

    assertThrows(IllegalArgumentException.class, () -> furnitureUCC.fixPurchasePrice(correctId, -15,
        correctState, correctDateCarryFromClient));
  }

  @Test
  @DisplayName("fixPurchasePrice : TestInexistingState")
  public void fixPurchasePriceTestInexistingState() {

    assertThrows(WrongStateException.class, () -> furnitureUCC.fixPurchasePrice(correctId,
        correctPurchasePrice, "ALLO", correctDateCarryFromClient));
  }

  @Test
  @DisplayName("fixPurchasePrice : TestCorrectWithENRES")
  public void fixPurchasePriceTestCorrectWithENRES() {

    FurnitureDTO f = furnitureUCC.fixPurchasePrice(correctId, correctPurchasePrice, "ENRES",
        correctDateCarryFromClient);

    assertAll(() -> assertNull(f.getDateCarryToStore()),
        () -> assertEquals(corrFurniture.getDescription(), f.getDescription()),
        () -> assertEquals("ENRES", f.getState()));

  }

  // carryToStore

  @Test
  @DisplayName("carryToStore : TestCorrect")
  public void carryToStoreTestCorrect() throws DALErrorException {
    FurnitureDTO initialFurnitureER = domainFactory.getFurniture();
    initialFurnitureER.setState("ENRES");
    Mockito.when(furnitureDAO.getFurniture(2)).thenReturn(initialFurnitureER);
    FurnitureDTO f = furnitureUCC.carryToStore(2);

    assertAll(() -> assertNotNull(f.getDateCarryToStore()),
        () -> assertEquals("ENMAG", f.getState()));

  }

  @DisplayName("carryToStore : TestInexistingId")
  public void carryToStoreTestInexistingId() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(0)).thenThrow(DALErrorException.class);

    assertThrows(InternalError.class, () -> furnitureUCC.carryToStore(0));

  }

  @Test
  @DisplayName("carryToStore : TestInexistingState")
  public void carryToStoreTestInexistingState() throws DALErrorException {
    FurnitureDTO initialFurnitureER = domainFactory.getFurniture();
    initialFurnitureER.setState("ALLO");
    Mockito.when(furnitureDAO.getFurniture(2)).thenReturn(initialFurnitureER);
    assertThrows(WrongStateException.class, () -> furnitureUCC.carryToStore(2));
  }


  // withdrawalFromSale

  @Test
  @DisplayName("withdrawalFromSale : testAllCorrect")
  public void withdrawalFromSaleTestAllCorrect() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(corrSellFurniture);
    FurnitureDTO returned = furnitureUCC.withdrawalFromSale(1);
    assertAll(() -> assertEquals(-1, returned.getSellPrice()),
        () -> assertEquals(-1, returned.getSpecialPrice()),
        () -> assertEquals("RETIR", returned.getState()));
  }

  @Test
  @DisplayName("withdrawalFromSale : testWrongState")
  public void withdrawalFromSaleTestWrongState() throws DALErrorException {
    corrSellFurniture.setState("ENRES");
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(corrSellFurniture);
    assertThrows(WrongStateException.class, () -> furnitureUCC.withdrawalFromSale(1));
  }

  @Test
  @DisplayName("withdrawalFromSale : testInexistingState")
  public void withdrawalFromSaleTestInexistingState() throws DALErrorException {
    corrSellFurniture.setState("RRRR");
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(corrSellFurniture);
    assertThrows(WrongStateException.class, () -> furnitureUCC.withdrawalFromSale(1));
  }

  @Test
  @DisplayName("withdrawalFromSale : testInexistingFurniture")
  public void withdrawalFromSaleTestInexistingFurniture() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(323232)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> furnitureUCC.withdrawalFromSale(323232));
  }

  @Test
  @DisplayName("withdrawalFromSale : testNullSpecialPrice")
  public void withdrawalFromSaleTestNullSpecialPrice() throws DALErrorException {
    corrSellFurniture.setSpecialPrice(-1);
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(corrSellFurniture);
    FurnitureDTO returned = furnitureUCC.withdrawalFromSale(1);
    assertAll(() -> assertEquals(-1, returned.getSellPrice()),
        () -> assertEquals(-1, returned.getSpecialPrice()),
        () -> assertEquals("RETIR", returned.getState()));
  }

  // getAllFurnitures

  @Test
  @DisplayName("getAllFurnitures : testNotEmptyList - Admin")
  public void getAllFurnituresTestNotEmptyListAdmin() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getAllFurnitures(true)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getAllFurnitures(true));
  }

  @Test
  @DisplayName("getAllFurnitures : testNotEmptyList - Other")
  public void getAllFurnituresTestNotEmptyListOther() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    corrFurniture.setState("ENVEN");
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getAllFurnitures(false)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getAllFurnitures(false));
  }

  @Test
  @DisplayName("getAllFurnitures : testNotEmptyListButNoENVENAndENOPT - Other")
  public void getAllFurnituresTestNotEmptyListButNoENVENAndENOPT() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getAllFurnitures(false)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getAllFurnitures(false));
  }

  @Test
  @DisplayName("getAllFurnitures : testEmptyList - Admin")
  public void getAllFurnituresTestEmptyListAdmin() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getAllFurnitures(true)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getAllFurnitures(true));
  }

  @Test
  @DisplayName("getAllFurnitures : testEmptyList - Other")
  public void getAllFurnituresTestEmptyListOther() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getAllFurnitures(false)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getAllFurnitures(false));
  }

  @Test
  @DisplayName("FixSellPrice : TestAllCorrect")
  public void fixSellPriceTestAllCorrect() {

    FurnitureDTO f = furnitureUCC.fixSellPrice(correctSellId, correctSellPrice, correctSpecialPrice,
        correctSellState);

    assertAll(() -> assertEquals(corrSellFurniture.getId(), f.getId()),
        () -> assertEquals(corrSellFurniture.getState(), f.getState()),
        () -> assertEquals(corrSellFurniture.getSpecialPrice(), f.getSpecialPrice()),
        () -> assertEquals(corrSellFurniture.getSellPrice(), f.getSellPrice()));
  }


  @Test
  @DisplayName("FixSellPrice : TestInexistingState")
  public void fixSellPriceTestInexistingState() {

    assertThrows(WrongStateException.class, () -> furnitureUCC.fixSellPrice(correctSellId,
        correctSellPrice, correctSpecialPrice, "CHEMISE"));
  }

  @Test
  @DisplayName("FixSellPrice : TestInexistingId")
  public void fixSellPriceTestInexistingId() {

    assertThrows(IllegalArgumentException.class, () -> furnitureUCC.fixSellPrice(0,
        correctSellPrice, correctSpecialPrice, correctSellState));
  }

  @Test
  @DisplayName("FixSellPrice : TestNegativeSellPrice")
  public void fixSellPriceTestNegativeSellPrice() {

    assertThrows(IllegalArgumentException.class,
        () -> furnitureUCC.fixSellPrice(correctSellId, -15, correctSpecialPrice, correctSellState));
  }

  // getFurniture

  @Test
  @DisplayName("GetFurniture : TestCorrectId as an admin")
  public void getFurnitureTestCorrectIdAsAnAdmin() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    FurnitureDTO receivedFurniture = furnitureUCC.getFurniture(correctId, userAdmin);
    assertEquals(corrFurniture, receivedFurniture);
  }

  @Test
  @DisplayName("GetFurniture : TestNegativeId as an admin")
  public void getFurnitureTestNegativeIdAsAnAdmin() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(-1)).thenThrow(DALErrorException.class);
    assertThrows(SomethingWentWrongException.class, () -> furnitureUCC.getFurniture(-1, userAdmin));
  }

  @Test
  @DisplayName("GetFurniture : TestCorrectId and furniture is in option")
  public void getFurnitureTestCorrectIdAndFurnitureIsInOption() throws DALErrorException {
    corrFurniture.setState("ENOPT");
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    FurnitureDTO receivedFurniture = furnitureUCC.getFurniture(correctId, corrUser);
    assertEquals(corrFurniture, receivedFurniture);
  }

  @Test
  @DisplayName("GetFurniture : TestCorrectId and furniture is in option and user is admin")
  public void getFurnitureTestCorrectIdAndFurnitureIsInOptionAndUserIsAdmin()
      throws DALErrorException {
    corrFurniture.setState("ENOPT");
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    FurnitureDTO receivedFurniture = furnitureUCC.getFurniture(correctId, userAdmin);
    assertEquals(corrFurniture, receivedFurniture);
  }

  @Test
  @DisplayName("GetFurniture : TestCorrectId and request as seller")
  public void getFurnitureTestCorrectIdAndRequestAsSeller() throws DALErrorException {
    corrFurniture.setState("PROPO");
    corrFurniture.setSeller(corrUser);
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    FurnitureDTO receivedFurniture = furnitureUCC.getFurniture(correctId, corrUser);
    assertEquals(corrFurniture, receivedFurniture);
  }

  @Test
  @DisplayName("GetFurniture : TestCorrectId and request as purchaser")
  public void getFurnitureTestCorrectIdAndRequestAsPurchaser() throws DALErrorException {
    corrFurniture.setState("VEN");
    corrFurniture.setPurchaser(corrUser);
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    FurnitureDTO receivedFurniture = furnitureUCC.getFurniture(correctId, corrUser);
    assertEquals(corrFurniture, receivedFurniture);
  }

  @Test
  @DisplayName("GetFurniture : TestCorrectId as client and furniture PROPO")
  public void getFurnitureTestCorrectIdAsClientAndFurniturePROPO() throws DALErrorException {
    corrFurniture.setState("PROPO");
    UserDTO seller = domainFactory.getUser();
    seller.setId(55);
    corrFurniture.setSeller(seller);
    Mockito.when(furnitureDAO.getFurniture(correctId)).thenReturn(corrFurniture);
    assertThrows(UnauthorizedException.class, () -> furnitureUCC.getFurniture(correctId, corrUser));
  }

  @Test
  @DisplayName("GetFurniture : Test no furniture with this id")
  public void getFurnitureTestNoFurnitureWithThisId() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(9999)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> furnitureUCC.getFurniture(9999, corrUser));
  }

  // createOption

  @Test
  @DisplayName("CreateOption : TestAllCorrect")
  public void createOptionTestAllCorrect() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(optionFurniture);
    Option o = furnitureUCC.createAnOption(option, 1, "ENOPT");

    assertAll(() -> assertEquals(optionFurniture.getState(), "ENOPT"),
        () -> assertEquals(option.getDuration(), o.getDuration()),
        () -> assertEquals(optionFurniture.getId(), o.getFurnitureId()),
        () -> assertFalse(o.getIsCancel()));
  }

  @Test
  @DisplayName("CreateOption : TestDurationAbove5")
  public void createOptionTestDurationAbove5() {
    option.setDuration(7);

    assertThrows(IncorrectDurationException.class,
        () -> furnitureUCC.createAnOption(option, 1, "ENOPT"));
  }

  @Test
  @DisplayName("CreateOption : TestInexistingState")
  public void createOptionTestInexistingState() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(optionFurniture);
    assertThrows(WrongStateException.class,
        () -> furnitureUCC.createAnOption(option, 1, "CHEMISE"));
  }

  @Test
  @DisplayName("CreateOption : TestInexistingFurniture")
  public void createOptionTestInexistingFurniture() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(323232)).thenReturn(null);
    assertThrows(NotFoundException.class,
        () -> furnitureUCC.createAnOption(option, 323232, "ENOPT"));
  }

  // remove option
  @Test
  @DisplayName("RemoveOption : TestAllCorrect")
  public void removeOptionTestAllCorrect() throws DALErrorException {
    Option o = furnitureUCC.removeAnOption(removeOption);

    assertAll(() -> assertEquals(removeOptionFurniture.getState(), "ENVEN"),
        () -> assertEquals(o.getDuration(), removeOption.getDuration()),
        () -> assertEquals(o.getFurnitureId(), removeOptionFurniture.getId()),
        () -> assertEquals(5, o.getDaysLeft()), () -> assertTrue(o.getIsCancel()));
  }

  @Test
  @DisplayName("RemoveOption : TestInexistingFurniture")
  public void removeOptionTestInexistingFurniture() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(43444)).thenReturn(null);
    removeOption.setFurnitureId(43444);
    assertThrows(NotFoundException.class, () -> furnitureUCC.removeAnOption(removeOption));
  }

  @Test
  @DisplayName("RemoveOption : TestInexistingUser")
  public void removeOptionTestInexistingUser() throws DALErrorException {
    Mockito.when(userDAO.getUser(9457974)).thenReturn(null);
    removeOption.setUserID(9457974);
    assertThrows(NotFoundException.class, () -> furnitureUCC.removeAnOption(removeOption));
  }

  @Test
  @DisplayName("RemoveOption : TestInexistingOption")
  public void removeOptionTestInexistingOption() throws DALErrorException {
    Option o = domainFactory.getOption();
    o.setFurnitureId(555);
    Mockito.when(furnitureDAO.getOption(o)).thenReturn(null);
    assertThrows(NotFoundException.class, () -> furnitureUCC.removeAnOption(o));
  }

  @Test
  @DisplayName("RemoveOption : TestInexistingState")
  public void removeOptionTestInexistingState() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(1)).thenReturn(optionFurniture);
    removeOptionFurniture.setState("JDN");
    assertThrows(WrongStateException.class, () -> furnitureUCC.removeAnOption(removeOption));
  }


  @Test
  @DisplayName("RemoveOption : TestAlreadyCancel")
  public void removeOptionTestAlreadyCancel() throws DALErrorException {
    Mockito.when(furnitureDAO.getFurniture(2)).thenReturn(removeOptionFurniture);
    removeOption.setIsCancel(true);
    assertThrows(AlreadyCancelException.class, () -> furnitureUCC.removeAnOption(removeOption));
  }

  // get option

  @Test
  @DisplayName("getAnOption : TestExistingOption")
  public void getAnOptionTestExistingOption() {
    Option o = domainFactory.getOption();
    try {
      Mockito.when(furnitureDAO.getOption(o)).thenReturn(o);
      o.setId(1);
    } catch (DALErrorException e) {
      e.printStackTrace();
    }
    furnitureUCC.getAnOption(o);
    assertEquals(1, o.getId());
  }

  @Test
  @DisplayName("getAnOption : TestInexistingOption")
  public void getAnOptionTestInexistingOption() {
    Option o = domainFactory.getOption();
    try {
      Mockito.when(furnitureDAO.getOption(o)).thenThrow(SomethingWentWrongException.class);
    } catch (DALErrorException e) {
      e.printStackTrace();
    }
    assertThrows(SomethingWentWrongException.class, () -> furnitureUCC.getAnOption(o));
  }

  // getFiltredFurnitures

  @Test
  @DisplayName("getFiltredFurnitures : testNotEmptyList - Admin")
  public void getFiltredFurnituresTestNotEmptyListAdmin() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getFiltredFurnitures(true, 10, 20, 2)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getFiltredFurnitures(true, 10, 20, 2));
  }

  @Test
  @DisplayName("getFiltredFurnitures : testNotEmptyList - Other")
  public void getFiltredFurnituresTestNotEmptyListOther() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    corrFurniture.setState("ENVEN");
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getFiltredFurnitures(false, 10, 20, 2)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getFiltredFurnitures(false, 10, 20, 2));
  }

  @Test
  @DisplayName("getFiltredFurnitures : testNotEmptyListButNoENVENAndENOPT - Other")
  public void getFiltredFurnituresTestNotEmptyListButNoENVENAndENOPT() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    furnitures.add(corrFurniture);
    Mockito.when(furnitureDAO.getFiltredFurnitures(false, 10, 20, 2)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getFiltredFurnitures(false, 10, 20, 2));
  }

  @Test
  @DisplayName("getFiltredFurnitures : testEmptyList - Admin")
  public void getFiltredFurnituresTestEmptyListAdmin() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getFiltredFurnitures(true, 10, 20, 2)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getFiltredFurnitures(true, 10, 20, 2));
  }

  @Test
  @DisplayName("getFiltredFurnitures : testEmptyList - Other")
  public void getFiltredFurnituresTestEmptyListOther() throws DALErrorException {
    List<FurnitureDTO> furnitures = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getFiltredFurnitures(false, 10, 20, 2)).thenReturn(furnitures);
    assertEquals(furnitures, furnitureUCC.getFiltredFurnitures(false, 10, 20, 2));
  }
  // add photo

  @Test
  @DisplayName("addPhoto : testAllCorrect")
  public void addPhotoTestAllCorrect() {
    Mockito.when(furnitureDAO.getFurniture(optionFurniture.getId())).thenReturn(optionFurniture);
    try {
      Mockito.when(furnitureDAO.insertPhoto(photo, optionFurniture.getId()))
          .thenReturn(photoAfterInsertion);
    } catch (DALErrorException e) {
      e.printStackTrace();
    }
    furnitureUCC.addPhoto(optionFurniture.getId(), file, photo.getPath());
    assertTrue(new File(photo.getPath()).exists());
  }

  @Test
  @DisplayName("addPhoto : testWrongState")
  public void addPhotoTestWrongState() {
    furnitureToInsertPhoto.setState("RETIR");
    Mockito.when(furnitureDAO.getFurniture(furnitureToInsertPhoto.getId()))
        .thenReturn(furnitureToInsertPhoto);
    assertThrows(WrongStateException.class,
        () -> furnitureUCC.addPhoto(furnitureToInsertPhoto.getId(), file, photo.getPath()));
  }

  // set favorite photo

  @Test
  @DisplayName("setFavoritePhoto : test all correct")
  public void setFavoritePhotoTestAllCorrect() {
    Mockito.when(furnitureDAO.getPhoto(photoFav.getId())).thenReturn(photoFav);

    FurnitureDTO furnitureReturned =
        furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId());
    assertEquals(furnitureReturned.getFavouritePhoto(), photoFav);
  }

  @Test
  @DisplayName("setFavoritePhoto : test incorrect id in parameter")
  public void setFavoritePhotoTestIncorrectIdInParameter() {
    furnitureToFavPhoto.setId(-1);
    assertThrows(NotFoundException.class,
        () -> furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId()));

    furnitureToFavPhoto.setId(9);
    photoFav.setId(-1);
    assertThrows(NotFoundException.class,
        () -> furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId()));
  }

  @Test
  @DisplayName("setFavoritePhoto : test photo to set favorite not in the list of photos")
  public void setFavoritePhotoTestPhotoToSetFavoriteNotInTheListOfPhotos() {
    furnitureToFavPhoto.setPhotos(new ArrayList<Photo>());
    assertThrows(NotFoundException.class,
        () -> furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId()));
  }

  @Test
  @DisplayName("setFavoritePhoto : test exception in dao handled when calling getPhoto")
  public void setFavoritePhotoTestExceptionInDaoHandledWhenCallingGetPhoto() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO).getPhoto(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class,
        () -> furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId()));
  }

  @Test
  @DisplayName("setFavoritePhoto : test exception in dao handled when calling updateFavoritePhoto")
  public void setFavoritePhotoTestExceptionInDaoHandledWhenCallingUpdateFavoritePhoto() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO).getPhoto(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class,
        () -> furnitureUCC.setFavoritePhoto(furnitureToFavPhoto, photoFav.getId()));
  }

  // research of furnitures

  @Test
  @DisplayName("getFurnituresByResearch : testAllCorrect")
  public void getFurnituresByResearchTestAllCorrect() {

    String testWord = "monMotClé";

    Mockito.when(furnitureDAO.getfurnituresByResearch(testWord))
        .thenReturn(new ArrayList<FurnitureDTO>());

    assertEquals(new ArrayList<FurnitureDTO>(), furnitureUCC.getFurnituresByResearch(testWord));
  }

  @Test
  @DisplayName("getFurnituresByResearch : testNoWord")
  public void getFurnituresByResearchTestNoWord() {

    String testWord = null;

    Mockito.when(furnitureDAO.getfurnituresByResearch(testWord))
        .thenReturn(new ArrayList<FurnitureDTO>());

    assertThrows(IllegalArgumentException.class,
        () -> furnitureUCC.getFurnituresByResearch(testWord));
  }

  // get tags

  @Test
  @DisplayName("getTags : testAllCorrect")
  public void getTagsTestAllCorrect() {


    Mockito.when(furnitureDAO.getTags()).thenReturn(new ArrayList<String>());

    assertEquals(new ArrayList<String>(), furnitureUCC.getTags());
  }


  // sell a furniture

  @Test
  @DisplayName("sellFurniture : testGoodSale")
  public void sellFurnitureTestGoodSale() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENVEN");
    f.setPurchaser(corrUser);
    f.setSpecialPrice(-1);

    FurnitureDTO furnitureChanged = domainFactory.getFurniture();
    furnitureChanged.setId(1);
    furnitureChanged.setState("VENDU");
    furnitureChanged.setPurchaser(corrUser);
    furnitureChanged.setSpecialPrice(-1);
    furnitureChanged.setDateSale(Date.valueOf(LocalDate.now()));

    Mockito.when(furnitureDAO.getFurniture(f.getId())).thenReturn(furnitureChanged);

    assertAll(() -> assertNotNull(furnitureUCC.sellFurniture(f).getDateSale()),
        () -> assertEquals(furnitureChanged.getState(), f.getState()));
  }

  @Test
  @DisplayName("sellFurniture : testWrongState")
  public void sellFurnitureTestWrongState() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("VENDU");
    f.setPurchaser(corrUser);
    f.setSpecialPrice(-1);

    assertThrows(WrongStateException.class, () -> furnitureUCC.sellFurniture(f));
  }

  @Test
  @DisplayName("sellFurniture : testSpecialPriceButClient")
  public void sellFurnitureTestSpecialPriceButClient() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENVEN");
    f.setPurchaser(corrUser);
    f.setSpecialPrice(100000);

    assertThrows(UnauthorizedException.class, () -> furnitureUCC.sellFurniture(f));
  }

  @Test
  @DisplayName("sellFurniture : testGoodSaleWithoutPurchase")
  public void sellFurnitureTestGoodSaleWithoutPurchase() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENVEN");
    f.setSpecialPrice(-1);

    FurnitureDTO furnitureChanged = domainFactory.getFurniture();
    furnitureChanged.setState("VENDU");
    furnitureChanged.setDateSale(Date.valueOf(LocalDate.now()));

    Mockito.when(furnitureDAO.getFurniture(f.getId())).thenReturn(furnitureChanged);

    assertAll(() -> assertNotNull(furnitureUCC.sellFurniture(f).getDateSale()),
        () -> assertEquals(furnitureChanged.getState(), f.getState()));
  }

  @Test
  @DisplayName("sellFurniture : test good sale to an antique dealer from state ENMAG")
  public void sellFurnitureTestGoodAntSaleFromStateENMAG() {
    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENMAG");
    f.setSpecialPrice(100000);
    f.setPurchaser(domainFactory.getUser());
    f.getPurchaser().setRole("ANT");

    FurnitureDTO furnitureChanged = domainFactory.getFurniture();
    furnitureChanged.setState("VENDU");
    furnitureChanged.setDateSale(Date.valueOf(LocalDate.now()));

    Mockito.when(furnitureDAO.getFurniture(f.getId())).thenReturn(furnitureChanged);

    assertAll(() -> assertNotNull(furnitureUCC.sellFurniture(f).getDateSale()),
        () -> assertEquals(furnitureChanged.getState(), f.getState()));
  }

  @Test
  @DisplayName("sellFurniture : test good sale to an antique dealer from state ENRES")
  public void sellFurnitureTestGoodAntSaleFromStateENRES() {
    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENRES");
    f.setSpecialPrice(100000);
    f.setPurchaser(domainFactory.getUser());
    f.getPurchaser().setRole("ANT");

    FurnitureDTO furnitureChanged = domainFactory.getFurniture();
    furnitureChanged.setState("VENDU");
    furnitureChanged.setDateSale(Date.valueOf(LocalDate.now()));

    Mockito.when(furnitureDAO.getFurniture(f.getId())).thenReturn(furnitureChanged);

    assertAll(() -> assertNotNull(furnitureUCC.sellFurniture(f).getDateSale()),
        () -> assertEquals(furnitureChanged.getState(), f.getState()));
  }

  @Test
  @DisplayName("sellFurniture : test wrong sale to an antique dealer from state PROPO")
  public void sellFurnitureTestWrongAntSaleFromStatePROPO() {
    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("PROPO");
    f.setSpecialPrice(100000);
    f.setPurchaser(domainFactory.getUser());
    f.getPurchaser().setRole("ANT");

    assertThrows(WrongStateException.class, () -> furnitureUCC.sellFurniture(f));
  }

  @Test
  @DisplayName("sellFurniture : test wrong sale to an client from state ENMAG")
  public void sellFurnitureTestWrongSaleClientFromStateENMAG() {
    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("ENMAG");
    f.setSpecialPrice(100000);
    f.setPurchaser(domainFactory.getUser());
    f.getPurchaser().setRole("CLI");

    assertThrows(WrongStateException.class, () -> furnitureUCC.sellFurniture(f));
  }


  // getAllFurnituresOfAVisit

  @Test
  @DisplayName("getAllFurnituresOfAVisit : test all correct")
  public void getAllFurnituresOfAVisitTestAllCorrect() {
    Mockito.when(furnitureDAO.getAllFurnituresOfAVisit(1)).thenReturn(furnituresOfAVisit);
    Mockito.when(userDAO.getUser(Mockito.anyInt())).thenReturn(corrUser);
    List<FurnitureDTO> furnituresReturned = furnitureUCC.getAllFurnituresOfAVisit(1);
    assertEquals(furnituresOfAVisit, furnituresReturned);
  }

  @Test
  @DisplayName("getAllFurnituresOfAVisit : incorrect id")
  public void getAllFurnituresOfAVisitTestIncorrectId() {
    assertThrows(NotFoundException.class, () -> furnitureUCC.getAllFurnituresOfAVisit(-1));
  }

  @Test
  @DisplayName("getAllFurnituresOfAVisit : test exception in dao handled")
  public void getAllFurnituresOfAVisitTestExceptionInDaoHandled() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO)
        .getAllFurnituresOfAVisit(Mockito.anyInt());
    assertThrows(SomethingWentWrongException.class, () -> furnitureUCC.getAllFurnituresOfAVisit(1));
  }


  // Test changeScrollable

  @Test
  @DisplayName("changeScrollable : testCallMethodInDAO")
  public void changeScrollableTestCallMethodInDAO() {
    furnitureUCC.changeScrollable(corrPhoto.getId(), corrPhoto.isScrollable());
    Mockito.verify(furnitureDAO).changeScrollable(corrPhoto.getId(), corrPhoto.isScrollable());
  }

  @Test
  @DisplayName("changeScrollable : testThrowError")
  public void changeScrollableTestThrowError() {
    assertThrows(IllegalArgumentException.class, () -> furnitureUCC.changeScrollable(-1, false));
  }

  // Test changeFavouritePhoto

  @Test
  @DisplayName("changeFavouritePhoto : testCallMethodInDAO")
  public void changeFavouritePhotoTestCallMethodInDAO() {
    furnitureUCC.changeFavouritePhoto(furnitureWithFavPhoto.getId(),
        furnitureWithFavPhoto.getFavouritePhoto().getId());
    Mockito.verify(furnitureDAO).changeFavouritePhoto(furnitureWithFavPhoto.getId(),
        furnitureWithFavPhoto.getFavouritePhoto().getId());
  }

  @Test
  @DisplayName("changeFavouritePhoto : testThrowError")
  public void changeFavouritePhotoTestThrowError() {
    assertThrows(IllegalArgumentException.class, () -> furnitureUCC.changeFavouritePhoto(-1, -1));
  }


  // test cancelVisitFurniture

  @Test
  @DisplayName("cancelVisitFurniture : test all correct")
  public void cancelVisitFurnitureTestAllCorrect() {
    assertDoesNotThrow(() -> furnitureUCC.cancelVisitFurniture(1));
  }

  @Test
  @DisplayName("cancelVisitFurniture : test id incorrect")
  public void cancelVisitFurnitureTestIdIncorrect() {
    assertThrows(IllegalArgumentException.class, () -> furnitureUCC.cancelVisitFurniture(-1));
  }

  @Test
  @DisplayName("cancelVisitFurniture : test DALError")
  public void cancelVisitFurnitureTestDALError() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO).cancelVisitFurniture(1);
    assertThrows(SomethingWentWrongException.class, () -> furnitureUCC.cancelVisitFurniture(1));
  }

  @Test
  @DisplayName("notSuitable : testGoodSale")
  public void notSuitableTestGoodSale() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("PROPO");

    assertEquals("PASCO", furnitureUCC.notSuitable(f).getState());
  }

  @Test
  @DisplayName("notSuitable : testWrongState")
  public void notSuitableTestWrongState() {

    FurnitureDTO f = domainFactory.getFurniture();
    f.setId(1);
    f.setState("VENDU");

    assertThrows(WrongStateException.class, () -> furnitureUCC.notSuitable(f));
  }

  // test createFurniture

  @Test
  @DisplayName("createFurniture : testAllCorrect")
  public void createFurnitureTestAllCorrect() {
    FurnitureUCC spy = Mockito.spy(furnitureUCC);
    Mockito.when(furnitureDAO.insertFurniture(furnitureToCreate, visit.getId()))
        .thenReturn(furnitureToCreate);
    Mockito.doReturn(photoAfterInsertion).when(spy).addPhoto(Mockito.eq(furnitureToCreate.getId()),
        Mockito.any(InputStream.class), Mockito.any(String.class));
    Mockito.doReturn(furnitureToCreate).when(spy).setFavoritePhoto(furnitureToCreate,
        photoAfterInsertion.getId());

    assertEquals(furnitureToCreate,
        spy.createFurniture(furnitureToCreate, visit, filesOfFurnitureToCreate));
  }

  @Test
  @DisplayName("createFurniture : test null furniture or visit")
  public void createFurnitureTestNullFurnitureOrVisit() {
    assertAll(
        () -> assertThrows(IllegalArgumentException.class,
            () -> furnitureUCC.createFurniture(null, visit, filesOfFurnitureToCreate)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> furnitureUCC.createFurniture(furnitureToCreate, null, filesOfFurnitureToCreate)));
  }

  @Test
  @DisplayName("createFurniture : test null furniture's type or furniture's description")
  public void createFurnitureTestNullFurnituresTypeOrFurnituresDescription() {
    furnitureToCreate.setType(null);
    assertThrows(IllegalArgumentException.class,
        () -> furnitureUCC.createFurniture(furnitureToCreate, visit, filesOfFurnitureToCreate));
    furnitureToCreate.setDescription(null);
    assertThrows(IllegalArgumentException.class,
        () -> furnitureUCC.createFurniture(furnitureToCreate, visit, filesOfFurnitureToCreate));
  }

  @Test
  @DisplayName("createFurniture : incorrect number of files")
  public void createFurnitureTestIncorrectNumberOfFiles() {
    filesOfFurnitureToCreate.remove(0);
    assertThrows(SomethingWentWrongException.class,
        () -> furnitureUCC.createFurniture(furnitureToCreate, visit, filesOfFurnitureToCreate));
  }

  @Test
  @DisplayName("createFurniture : test DALError")
  public void createFurnitureTestDALError() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO).insertFurniture(furnitureToCreate,
        visit.getId());
    assertThrows(SomethingWentWrongException.class,
        () -> furnitureUCC.createFurniture(furnitureToCreate, visit, filesOfFurnitureToCreate));
  }

  // test verifyAllOptions

  @Test
  @DisplayName("verifyAllOptions : test 1 option expired among two")
  public void verifyAllOptionsTest1OptionExpiredAmongTwo() {
    List<Option> options = new ArrayList<Option>();
    option.setLimitDate(Date.valueOf(LocalDate.now().minusDays(1)));
    options.add(option);
    options.add(removeOption);

    FurnitureUCC spy = Mockito.spy(furnitureUCC);
    Mockito.doReturn(option).when(spy).removeAnOption(option);
    Mockito.doReturn(removeOption).when(spy).removeAnOption(removeOption);
    Mockito.when(furnitureDAO.getAllOptionsNotCanceled()).thenReturn(options);
    assertEquals(1, spy.verifyAllOptions());
  }

  @Test
  @DisplayName("verifyAllOptions : test 2 options expired among two")
  public void verifyAllOptionsTest2OptionsExpiredAmongTwo() {
    List<Option> options = new ArrayList<Option>();
    option.setLimitDate(Date.valueOf(LocalDate.now().minusDays(1)));
    options.add(option);
    removeOption.setLimitDate(Date.valueOf(LocalDate.now().minusDays(2)));
    options.add(removeOption);

    FurnitureUCC spy = Mockito.spy(furnitureUCC);
    Mockito.doReturn(option).when(spy).removeAnOption(option);
    Mockito.doReturn(removeOption).when(spy).removeAnOption(removeOption);
    Mockito.when(furnitureDAO.getAllOptionsNotCanceled()).thenReturn(options);
    assertEquals(2, spy.verifyAllOptions());
  }

  @Test
  @DisplayName("verifyAllOptions : test 0 option expired among two")
  public void verifyAllOptionsTest0OptionExpiredAmongTwo() {
    List<Option> options = new ArrayList<Option>();
    options.add(option);
    options.add(removeOption);

    FurnitureUCC spy = Mockito.spy(furnitureUCC);
    Mockito.doReturn(option).when(spy).removeAnOption(option);
    Mockito.doReturn(removeOption).when(spy).removeAnOption(removeOption);
    Mockito.when(furnitureDAO.getAllOptionsNotCanceled()).thenReturn(options);
    assertEquals(0, spy.verifyAllOptions());
  }

  @Test
  @DisplayName("verifyAllOptions : test DALError")
  public void verifyAllOptionsTestDALError() {
    Mockito.doThrow(DALErrorException.class).when(furnitureDAO).getAllOptionsNotCanceled();
    assertThrows(SomethingWentWrongException.class, () -> furnitureUCC.verifyAllOptions());
  }


  @Test
  @DisplayName("addType : testGoodAdd")
  public void addTypeTestGoodAdd() {

    Mockito.when(furnitureDAO.addType("Bureau")).thenReturn(1);

    assertEquals(1, furnitureUCC.addType("Bureau"));
  }

}
