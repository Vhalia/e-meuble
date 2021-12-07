import { getUserStorageData } from "../Utils/storage";
import findGetParameter from "../Utils/urlUtil";
import { displayErrorOnErrorPage, displayErrorOnThisPage } from "./Error";
import displayNavBar from "./NavBar";
import { displayUserData, displayFurnitureWithOptionHTML, getCoordinateFromAddress, displayBoughtFurnitures, displaySoldFurnitures} from "./Profil";

let user;

function displayViewProfil() {
 
    user = getUserStorageData();
    $("#main").empty()
    $("#main").append(`
    <div id="user-data">
        </div>
        <div id="mapContainer" class="row justify-content-center mb-5 mt-5">
            <div id="mapProfil"></div>
        </div>
        <div id="sold-bought-furnitures">
            <div id="profil-bought-furnitures" class="text-center"></div>
            <div id="profil-sold-furnitures" class="text-center"></div>
        </div>
        <div id="user-options">
        </div>
    `)
    displayNavBar()
    fetch("/api/users/"+ findGetParameter("idUser"), {
        method: "GET",
        headers:{Authorization: user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => {
        displayProfilData(data)
        getCoordinateFromAddress(data.address)
    })
    .catch((err) => displayErrorOnErrorPage(err))
}

function displayProfilData(data) {
    displayUserData(data);

    fetch("/api/users/furnitureOption/"+findGetParameter("idUser"), {
        method : "GET",
        headers : {Authorization : user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => {
        displayFurnitureWithOptionHTML(data)
        $("#profil-text-noOption").text("Aucune option n'a été émise par cet utilisateur.")
        $("#btn-cancel-option").on("click", onCancelingOption)
        getFurnitureboughtAndSold()
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function getFurnitureboughtAndSold() {
    fetch("/api/users/boughtFurnitures/" + findGetParameter("idUser"), {
        method: "GET",
        headers: {
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => {
        displayBoughtFurnitures(data)
        $("#profil-text-noBoughtFurniture").text("Aucun meuble n'a été acheté par cet utilisateur.")
    })
    .catch((err) => displayErrorOnThisPage(err))

    fetch("/api/users/soldFurnitures/" + findGetParameter("idUser"), {
        method: "GET",
        headers: {
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => {
        displaySoldFurnitures(data)
        $("#profil-text-noSoldFurniture").text("Aucun meuble n'a été vendu par cet utilisateur.")
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function onCancelingOption(e) {
    e.preventDefault()
    fetch("/api/furnitures/removeOption/" + e.target.dataset.id, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => displayViewProfil())
    .catch((err) => displayErrorOnThisPage(err))
}

export default displayViewProfil;