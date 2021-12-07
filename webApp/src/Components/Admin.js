import { getUserStorageData } from "../Utils/storage"
import {displayErrorOnThisPage} from "./Error"
import {convertToLocalDate } from "../Utils/dateUtil.js";
import displayNavBar from "./NavBar"
import { onNavigate, redirectUrl } from "./Router"

function displayAdmin() {
    $("#main").empty()
    displayUsersNotValidated()
    displayNavBar()
    displayAddTypeForm()
}

let user;

function displayAddTypeForm(){
    $("#main").append(`
    <form id="newTypeForm">   
        <input type="text" placeholder="nouveau type"  class="form-control mt-1" id="newType">
        <input id="submit-newType"  class="form-control mt-2" type="submit" value="ajouter">
    </form>
    `);

    $("#newTypeForm").on("submit", sendNewType);
}

function sendNewType(e){
    let type = $("#newType").val();
    e.preventDefault();
    fetch("/api/furnitures/addType/", {
        method : "POST",
        body : type,
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => displayAdmin())
    .catch((err) => displayErrorOnThisPage(err)) 

}

function displayUsersNotValidated() {
    user = getUserStorageData()
    if (!user) displayErrorOnThisPage(new Error("Vous devez être connecté et être un admin pour accéder à cette ressource"))
    fetch("/api/users/usersNotValidated", {
        method: "GET",
        headers: {Authorization : user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText)
        return response.json();
    })
    .then((data) => {
            getAllVisits()
            onGettingListUsersNotValidated(data)
        }
    )
    .catch((err) => displayErrorOnThisPage(err))
}

function onGettingListUsersNotValidated(data) {
    $("#main").append(`
    <table id="table-usersNotValidated" class="table table-striped table-dark container mt-5">
        <thead>
            <tr>
                <th scope="col">pseudo</th>
                <th scope="col">nom</th>
                <th scope="col">prenom</th>
                <th scope="col">email</th>
                <th scope="col">adresse</th>
                <th scope="col">role demandé</th>
                <th scope="col"></th>
                <th scope="col"></th>
            </tr>
        </thead>
        <tbody id="usersNotValidatedListBody"></tbody>
    </table>`)
    data.forEach(user => {
        $("#usersNotValidatedListBody").append(`
        <tr data-url="/viewProfil" data-id="${user.id}" data-querykey="idUser">
            <td scope="row" id="listUserNotValidatedUsername">${user.userName}</td>
            <td>${user.name}</td>
            <td>${user.surname}</td>
            <td>${user.email}</td>
            <td>${user.address.nbr} ${user.address.street}, ${user.address.postalCode}</td>
            <td>${user.role}</td>
            <td><button data-role=${user.role} class="accept-userNotValidated btn btn-success rounded-circle">V</button></td>
            <td><button class="refuse-userNotValidated btn btn-danger rounded-circle">X</button></td>
        </tr>
        `)
    });
    if ($('#usersNotValidatedListBody *').length == 0) {
        $("#table-usersNotValidated").empty()
        $('#table-usersNotValidated').after("<p class='container background-dark text-white p-2 text-center w-25'>Il n'y a aucune demande de rôle pour le moment</p>")
    }
    $(".accept-userNotValidated").on("click", onAcceptUsersRegistration)
    $(".refuse-userNotValidated").on("click", onRefuseUsersRegistration)
    $("#usersNotValidatedListBody tr").on("click", onNavigate);
}

function onAcceptUsersRegistration(e) {
    e.preventDefault()

    let usernameOfUserWhoRequested = {
        userName : $("#listUserNotValidatedUsername").html()
    }

    fetch("/api/users/validateRegister?roleRequested="+e.target.dataset.role, {
        method: "PUT",
        body: JSON.stringify(usernameOfUserWhoRequested),
        headers : {"Content-Type" : "application/json", Authorization:user.userCo.token}
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText)
        redirectUrl("/admin")
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function onRefuseUsersRegistration(e) {
    e.preventDefault()

    let usernameOfUserWhoRequested = {
        userName : $("#listUserNotValidatedUsername").html()
    }

    fetch("/api/users/refuseRegister", {
        method: "PUT",
        body: JSON.stringify(usernameOfUserWhoRequested),
        headers : {"Content-Type" : "application/json", Authorization:user.userCo.token}
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText)
        redirectUrl("/admin")
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function getAllVisits() {
    fetch("/api/visits/",{
        method: "GET",
        headers : {Authorization : user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText)
        return response.json()
    })
    .then((data) => displayAllVisits(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayAllVisits(data) {
    $("#main").append(`
    <table id="table-visits" class="table table-striped table-dark container mt-5">
        <thead>
            <tr>
                <th scope="col">Etat</th>
                <th scope="col">Date de demande</th>
                <th scope="col">Date de visite</th>
                <th scope="col">Pseudo</th>
            </tr>
        </thead>
        <tbody id="visitsListBody"></tbody>
    </table>`)
    data.forEach(visit => {
        let requestDate = visit.visitDateTime
        if (!requestDate) requestDate = "/"
        $("#visitsListBody").append(`
        <tr data-url="/visit" data-id="${visit.id}" data-querykey="idVisit">
            <td scope="row">${visit.state}</td>
            <td>${convertToLocalDate(visit.requestDate)}</td>
            <td>${requestDate}</td>
            <td>${visit.client.userName}</td>
        </tr>
        `)
    });
    if ($('#visitsListBody *').length == 0) {
        $("#table-visits").empty()
        $('#table-visits').after("<p class='container background-dark text-white p-2 text-center w-25'>Il n'y a aucune visite</p>")
    }
    $("#visitsListBody tr").on("click", onNavigate);
}

export default displayAdmin