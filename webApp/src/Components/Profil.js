import {
    convertToLocalDate
} from "../Utils/dateUtil";
import {
    getUserStorageData
} from "../Utils/storage";
import {
    displayErrorOnThisPage
} from "./Error";
import displayNavBar from "./NavBar"
import { onNavigate} from "./Router";

import H from "@here/maps-api-for-javascript";

let user;

function displayProfil() {
    $("#main").empty()
    displayNavBar()

    $("#main").append(`
        <div id="user-data">
        </div>
        <div id="mapContainer" class="row justify-content-center mb-5 mt-5">
            <div id="mapProfil"></div>
        </div>
        <div id="sold-bought-furnitures">
        </div>
        <div id="user-options">
        </div>
        <div id="visitsTab"></div>
    `)

    user = getUserStorageData();
    fetch("/api/users/me", {
            method: "GET",
            headers: {
                Authorization: user.userCo.token
            }
        })
        .then((response) => {
            if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
            return response.json();
        })
        .then((data) => {
            getCoordinateFromAddress(data.user.address)
            displayUserDataAndOptionsAndFurnituresAndVisits(data.user)
        })
        .catch((err) => displayErrorOnThisPage(err))
}

function displayUserDataAndOptionsAndFurnituresAndVisits(data) {
    displayUserData(data);

    fetch("/api/users/furnitureOption/" + user.userCo.user.id, {
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
            displayFurnitureWithOption(data);
            displayBoughtSoldFurnitures();
            displayVisits();
        })
        .catch((err) => displayErrorOnThisPage(err))
    
}

function displayUserData(data) {
    let roleToDisplay = "Client"
    if (data.registrationValidated && data.role == "ADM") {
        roleToDisplay = "Administrateur"
    } else if (data.registrationValidated && data.role == "ANT") {
        roleToDisplay = "Antiquaire"
    }
    $("#main").append(`<div id= "user-data"> </div>`)
    $("#user-data").append(`
    <div id="profil-info" class="d-flex flex-row justify-content-around mt-5">
        <div class="d-flex flex-column">
            <h1>Générales:</h1>
            <div><hr class="hr-underline-title"></div>
            <p class="text-white">Nom: <span class="font-weight-bold">${data.name}</span></p>
            <div><hr></div>
            <p class="text-white">Prenom: <span class="font-weight-bold">${data.surname}</span></p>
            <div><hr></div>
            <p class="text-white">Email: <span class="font-weight-bold">${data.email}</span></p>
            <div><hr></div>
            <p id="profil-role" class="text-white">Role: <span class="font-weight-bold">${roleToDisplay}</span></p>
            <div><hr></div>
            <p class="text-white">Pseudo: <span class="font-weight-bold">${data.userName}</span></p>
            <div><hr></div>
            <p class="text-white">Date inscription: <span class="font-weight-bold">${data.registrationDate}</span></p>
            <div><hr></div>
        </div>
        <div class="d-flex flex-column">
            <h1>Adresse:</h1>
            <div><hr class="hr-underline-title"></div>
            <p class="text-white">Code postal: <span class="font-weight-bold">${data.address.postalCode}</span></p>
            <div><hr></div>
            <p class="text-white">Rue: <span class="font-weight-bold">${data.address.street}</span></p>
            <div><hr></div>
            <p class="text-white">Numéro: <span class="font-weight-bold">${data.address.nbr}</span></p>
            <div><hr></div>
            <p class="text-white">Commune: <span class="font-weight-bold">${data.address.commune}</span></p>
            <div><hr></div>
            <p class="text-white">Pays: <span class="font-weight-bold">${data.address.country}</span></p>
            <div><hr></div>
        </div>
    </div>
    `)
    if (!data.registrationValidated) {
        let roleRequested = "Administrateur"
        if (data.role == "ANT") roleRequested = "Antiquaire"
        $("#profil-role").after(`
        <div><hr></div>
        <p class="text-white">Role demandé: <span class="font-weight-bold">${roleRequested}</span></p>
        `)
    }
}

function displayFurnitureWithOptionHTML(data) {
    $("#user-options").append(`
    <div id="profil-furnitures-option" class="text-center">
        <h1>Options: </h1>
        <div><hr class="hr-underline-title w-25"></div>
        <div id="profil-furnitures-option-content" class="d-flex justify-content-around">
        </div>
    </div>
    `)
    data.forEach((furniture) => {
        $("#profil-furnitures-option-content").append(`
    <div>
        <div>
            <button id="btn-cancel-option" data-id="${furniture.id}" class="btn btn-danger rounded-circle">X</button>
            <img class="rounded" src="data:image/${furniture.favouritePhoto.extension};base64,${furniture.favouritePhoto.bytes}" height="150" width="150" alt="furniture-image"></img>
        </div>
        <div id="bg-card-furniture" class="mt-1">
            <p class="text-white rounded">${furniture.description}</p>
         </div>
    </div>
    `)
    })

    if (data.length == 0) {
        $("#profil-furnitures-option hr").append(`
        <p id="profil-text-noOption" class='container background-dark text-white p-2 text-center mt-5'>Vous n'avez pas mis d'option sur un meuble.</p>
    `)
    }
}

function displayFurnitureWithOption(data) {
    displayFurnitureWithOptionHTML(data)
    $("#btn-cancel-option").on("click", onCancelingOption)
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
        .then(() => displayProfil())
        .catch((err) => displayErrorOnThisPage(err))
}

function displayBoughtSoldFurnitures() {
    $("#sold-bought-furnitures").append(`
    <div id="profil-bought-furnitures" class="text-center"></div>
    <div id="profil-sold-furnitures" class="text-center"></div>
    `)

    fetch("/api/users/boughtFurnitures/" + user.userCo.user.id, {
        method: "GET",
        headers: {
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => displayBoughtFurnitures(data))
    .catch((err) => displayErrorOnThisPage(err))

    fetch("/api/users/soldFurnitures/" + user.userCo.user.id, {
        method: "GET",
        headers: {
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => displaySoldFurnitures(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayBoughtFurnitures(data){
    $("#profil-bought-furnitures").append(`
    <h1>Meubles achetés : </h1>
    <div><hr class="hr-underline-title w-25"></div>
    <div id="profil-bought-furnitures-content" class="d-flex justify-content-around">
    </div>
    `)
    data.forEach((furniture) => {
        $("#profil-bought-furnitures-content").append(`
    <div>
        <div data-url="/furnitures" data-id="${furniture.id}">
            <img class="imageProfilFClient" class="rounded" src="data:image/${furniture.favouritePhoto.extension};base64,${furniture.favouritePhoto.bytes}" height="150" width="150" alt="furniture-image"></img>
        </div>
        <div id="bg-card-furniture" class="mt-1">
            <p class="text-white rounded">${furniture.description}</p>
            <p id="sellPriceProfilClient${furniture.id}" class="text-white rounded"></p>
         </div>
    </div>
    `)
    if(furniture.specialPrice != -1){
        $("#sellPriceProfilClient"+furniture.id).append(`${furniture.specialPrice}€`);
    }
    else if(furniture.sellPrice != -1){
        $("#sellPriceProfilClient"+furniture.id).append(`${furniture.sellPrice}€`);
    }
    })

    if (data.length == 0) {
        $("#profil-bought-furnitures-content").append(`
        <p id="profil-text-noBoughtFurniture" class='container background-dark text-white p-2 text-center mt-5'>Vous n'avez pas de meubles achetés.</p>
    `)
    }
}

function displaySoldFurnitures(data){
    $("#profil-sold-furnitures").append(`
    <h1>Meubles proposé, vendus ou ne convenant pas: </h1>
    <div><hr class="hr-underline-title w-25"></div>
    <div id="profil-sold-furnitures-content" class="d-flex justify-content-around">
    </div>
    `)
    data.forEach((furniture) => {
        $("#profil-sold-furnitures-content").append(`
    <div>
        <div data-url="/furnitures" data-id="${furniture.id}">
            <img class="imageProfilFClient" class="rounded" src="data:image/${furniture.favouritePhoto.extension};base64,${furniture.favouritePhoto.bytes}" height="150" width="150" alt="furniture-image"></img>
        </div>
        <div id="bg-card-furniture" class="mt-1">
            <p class="text-white rounded">${furniture.description}</p>
            <p id="purchasePriceProfilClient${furniture.id}" class="text-white rounded"></p>
         </div>
    </div>
    `)
    if(furniture.purchasePrice != -1){
        $("#purchasePriceProfilClient"+furniture.id).append(`${furniture.purchasePrice}€`);
    }
    })

    if (data.length == 0) {
        $("#profil-sold-furnitures-content").append(`
        <p id="profil-text-noSoldFurniture" class='container background-dark text-white p-2 text-center mt-5'>Vous n'avez pas de meubles vendus.</p>
    `)
    }
    $(".imageProfilFClient").on("click", onNavigate)
}

function getCoordinateFromAddress(address) {
    let street = address.street.replaceAll(" ", "+")
    let country = address.country.replaceAll(" ", "+")
    fetch(`https://geocode.search.hereapi.com/v1/geocode?qq=country=${country};street=${street};postalCode=${address.postalCode};houseNumber=${address.nbr}&apiKey=iZ-2P9kgHWQ0HSG039LYxnOemXf-GsW5p4hyXHmyYjE`)
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => loadMap(data.items[0].position))
    .catch((err) => displayErrorOnThisPage(err))
}

function loadMap(coordinates) {
    //Initlialise la communication avec l'api grace à la clé du projet donné par HERE maps.
    let platform = new H.service.Platform({
        apikey: 'iZ-2P9kgHWQ0HSG039LYxnOemXf-GsW5p4hyXHmyYjE'
    });
    let defaultLayers = platform.createDefaultLayers();
  
    //Initialise la carte
    let map = new H.Map(document.getElementById('mapProfil'),
        defaultLayers.vector.normal.map, {
        center: coordinates,
        zoom: 15
    });

    // Ajout d'un event listener lorqu'on zoom in/out sur la map pour s'assurer que la map garde le même espace au sein de sa div.
    window.addEventListener('resize', () => map.getViewPort().resize());
    
    // Rend la carte interactible
    // MapEvents active le system d'evenements
    // Behavior implémetente les interactions de bases (zoom, ...)
    let behavior = new H.mapevents.Behavior(new H.mapevents.MapEvents(map));
    
    // Crée l'UI par défaut de la map
    let ui = H.ui.UI.createDefault(map, defaultLayers, 'fr-FR');
    
}

function displayVisits(){
    fetch("/api/visits/getVisits/" + user.userCo.user.id, {
        method: "GET",
        headers: {
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => displayVisitsHtml(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayVisitsHtml(data){
    $("#visitsTab").append(`
    <h1 class="visit-title">Vos visites: </h1>
    <div><hr class="hr-underline-title w-25"></div>
    <table class="table table-striped table-dark container mt-2"> 
        <thead>
            <tr>
                <th>Date de demande</th>
                <th>Etat</th>
                <th>raison d'annulation</th>
                <th>date de visite prévue</th>
            </tr>
        </thead>
        <tbody id=contentTableVisits></tbody>
    </table>
    `);

    data.forEach(visit => {
        $("#contentTableVisits").append(`
        <tr data-url="/visitRequest" data-id="${visit.id}">
            <td>${convertToLocalDate(visit.requestDate)}</td>
            <td>${visit.state}</td>
            <td id="annu${visit.id}"></td>
            <td id="dateV${visit.id}"></td>
        </tr>
        `);
        if(visit.cancellationNote) $("#annu" + visit.id).append(`${visit.cancellationNote}`);
        if(visit.visitDateTime) $("#dateV" + visit.id).append(`${visit.visitDateTime}`);
    })
    
}


export {
    displayProfil,
    displayUserData,
    displayFurnitureWithOptionHTML,
    getCoordinateFromAddress,
    loadMap,
    displayBoughtFurnitures,
    displaySoldFurnitures
}