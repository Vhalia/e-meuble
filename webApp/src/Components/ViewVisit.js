import { getUserStorageData } from "../Utils/storage"
import findGetParameter from "../Utils/urlUtil"
import { convert, convertToLocalDate } from "../Utils/dateUtil.js";
import { displayErrorOnErrorPage, displayErrorOnThisPage } from "./Error"
import displayNavBar from "./NavBar"
import { onNavigate } from "./Router"
const jwt = require("jsonwebtoken")

function displayVisit() {
    $("#main").empty()
    displayNavBar()
    getVisitById()
}

let user;
let usersTokenData;
let visitRetrieved;

function getVisitById() {
    user = getUserStorageData()
    if (!user){
        displayErrorOnErrorPage(new Error("Vous devez être connecté pour accéder à la page d'une visite"))
        return;
    } 
    usersTokenData = jwt.decode(user.userCo.token)
    let id = findGetParameter("idVisit");
    fetch("/api/visits/"+id, {
        method : "GET",
        headers : {Authorization : user.userCo.token}
    })
    .then((response) => {
        if(!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => onGettingVisit(data))
    .catch((err) => displayErrorOnErrorPage(err))
}

function onGettingVisit(data) {
    visitRetrieved = data
    $("#main").empty()
    $("#main").append(`
    <div id="viewVisitRequest" class="col-12">
        <h1 class="row justify-content-center font-weight-bold">Demande de visite</h1>
        <div><hr class="hr-underline-title w-25"></div>
        <div class="row justify-content-between mt-5">
            <div id="firstPartViewVisit" class="col-3">
                <h3 class="row text-white justify-content-center font-weight-bold">Date de la demande:</h3>
                <h4 class="row text-white justify-content-center">${convertToLocalDate(data.requestDate)}</h4>
                <h3 class="row text-white justify-content-center font-weight-bold">Plage horaire des disponibilités:</h3>
                <h4 class="row text-white justify-content-center">${data.usersTimeSlot}</h4>
            </div>
            <div id="furnituresPart" class="col-6">
                <h3 class="row text-white justify-content-center font-weight-bold mb-5">Meubles proposés:</h3>
            </div>
            <div class="col-3">
                <h3 class="row text-white justify-content-center font-weight-bold">Pseudo client: </h3>
                <h4 class="row text-white justify-content-center">${data.client.userName}</h4>
                
                <h3 class="row text-white font-weight-bold justify-content-center">Adresse du lieu d'entreposage:</h3>
                <h4 class="row text-white justify-content-center">${data.storageAddress.street}, ${data.storageAddress.nbr}</h4>
                <h4 class="row text-white justify-content-center">${data.storageAddress.postalCode} ${data.storageAddress.commune}</h4>
                <h4 class="row text-white justify-content-center">${data.storageAddress.country}</h4>
            </div>
        </div>
        <div  id ="ChangeStateVisit" class="row justify-content-end mt-5"></div>
    </div>
    `)

    if(data.state == "DEM"){
        $("#ChangeStateVisit").append(`
        <button id="btnCancelVisitRequest" class="btn btn-secondary mr-5">Annuler la demande</button>
        <button id="ValiderLaDemande" class="btn btn-primary mr-1">Valider la demande</button>
    `);
    }

    displayEachFurnitureInVisit(data.furnitures)
    $("#ValiderLaDemande").on("click", displayFormConfVisit);
    $("#btnCancelVisitRequest").on("click", onClickingCancelVisitRequest)
}

function displayFormConfVisit(e){
    e.preventDefault();
    $("#ChangeStateVisit").empty();
    $("#ChangeStateVisit").append(`
        <form id="confVisitForm"> 
            <input type="datetime-local" id="localDateTimeVisit">
            <input type="submit" id="sendConfVisit">
        </form>
    `);
    $("#confVisitForm").on("submit", confVisit);
}

function confVisit(e){
    e.preventDefault();

    visitRetrieved.visitDateTime = $("#localDateTimeVisit").val();
    let time = visitRetrieved.visitDateTime.split("T");
    time[0] = convert(time[0])
    time[1] = time[1] + ":00"

    visitRetrieved.visitDateTime = time[0] + " " + time[1]

    fetch("/api/visits/confirm", {
        method : "POST",
        body : JSON.stringify(visitRetrieved),
        headers : {
            "Content-Type" : "application/json",
            Authorization : user.userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onGettingVisit(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayEachFurnitureInVisit(furnitures) {
    furnitures.forEach((furniture) => {
        $("#furnituresPart").append(`
        <div class="row justify-content-center align-items-center">
                <div class="col-3 textFurnitureVisit" data-url="/furnitures" data-id="${furniture.id}" data-querykey="idMeuble" data-aim="viewFurniturePage">
                    <h5 class="text-white">${furniture.type}</h5>
                    <p class="text-white">${furniture.description}</p>
                </div>
                <div class="col-3 imageFurnitureVisit" data-url="/furnitures" data-id="${furniture.id}" data-querykey="idMeuble" data-aim="viewFurniturePage">
                    <img class="img-fluid" alt="image du meuble" src="data:image/${furniture.favouritePhoto.extension};base64,${furniture.favouritePhoto.bytes}">
                </div>
            </div>
        </div>
        <hr class="w-75">
        `)
    })
    $(".textFurnitureVisit, .imageFurnitureVisit").on("click", onNavigate)
}

function onClickingCancelVisitRequest(e) {
    e.preventDefault()

    $("#main").append(`
        <div id="formEnCancelVisitRequest">
        <h3>Changer d'état le meuble: </h3>
        <form id="enVenForm">
            <input type="text" class="form-control mt-2" id="formCancelNoteText" placeholder="motif d'annulation">
            <input type="submit" class="form-control mt-2" id="formCancelNoteSubmit" value="Confirmer">
        </form>
        </div>
    `)

    $("#formEnCancelVisitRequest").on("submit", (e) => {
        e.preventDefault()
        if ($("#formCancelNoteText").val()) {
            onClickingConfirmCancelVisit()
        }
    })
}

function onClickingConfirmCancelVisit() {
    let visit = {
        id : visitRetrieved.id,
        state : visitRetrieved.state,
        cancellationNote : $("#formCancelNoteText").val()
    }
    fetch("/api/visits/cancelVisit", {
        method: "POST",
        headers : {
            "Content-Type" : "application/json",
            Authorization : user.userCo.token
        },
        body : JSON.stringify(visit)
    })
    .then((response) => {
        if (!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => {
        displayVisit()
        $("#firstPartViewVisit").append(`
            <h3 class="row text-white justify-content-center font-weight-bold">Motif d'annulation:</h3>
            <h4 class="row text-white justify-content-center">${data.cancellationNote}</h4>
        `)
    })
    .catch((err) => displayErrorOnThisPage(err))
}

export default displayVisit