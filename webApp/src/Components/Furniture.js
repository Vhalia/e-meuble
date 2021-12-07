import { getUserStorageData } from "../Utils/storage.js";
import displayNavbar from "./NavBar.js";
import { onNavigate } from "./Router.js";
import { displayErrorOnThisPage } from "./Error.js";
import { convertToLocalDate, convert } from "../Utils/dateUtil.js";
import findGetParameter from "../Utils/urlUtil.js";
const jwt = require("jsonwebtoken")



let furnitureData;

function displayFurniture() {

    $("#navbar").empty();
    displayNavbar();
    getFurnitureData();

}

let user;
let usersTokenData;


function getFurnitureData() {
    user = getUserStorageData()
    if (!user){
        onErrorGettingFurniture(new Error("Vous devez être connecté pour accéder à la page d'un meuble"))
        return;
    } 
    usersTokenData = jwt.decode(user.userCo.token)
    let id = findGetParameter("idMeuble");
    fetch("/api/furnitures/" + id, {
        method: "GET",
        headers: {
            Authorization: user.userCo.token,
        },
    })
        .then((response) => {
            if (!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
            return response.json();
        })
        .then((data) => displayFurnitureData(data))
        .catch((err) => onErrorGettingFurniture(err))
}

function displayFurnitureData(data) {
    $("#main").empty();
    $("#main").append(`<div id="changeStateForm"></div>`);
    furnitureData = data;

    let furnitureState = furnitureData.state;
    let furnitureStateString;
    let badgeBgClass = "badge-bg-default";
    let isRightStateToAddPhoto = false;
    switch (furnitureState){
        case "PROPO":
            furnitureStateString = "Proposé";
            badgeBgClass = "badge-bg-brownyellow";
            if(usersTokenData.isAdmin){
                fetch("/api/visits/" + furnitureData.visitId, {
                    method: "GET",
                    headers: {
                        Authorization: user.userCo.token,
                    },
                })
                    .then((response) => {
                        if (!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
                        return response.json();
                    })
                    .then((data) => {
                        if(data.state == "CONF"){
                            $("#changeStateForm").append(`
                                <button id="propButton" type="button" class="btn btn-secondary ml-5 mt-5">Mettre en restauration/en magasin</button>
                                <button id="toPasCo" type="button" class="btn btn-secondary ml-5 mt-5">Ne convient pas</button>

                                `);
                            $("#propButton").on("click",displayPropoForm);
                            $("#toPasCo").on("click",sendNotSuitable);
                        }
                    })
                    .catch((err) => onErrorGettingFurniture(err))
            }
            break;
        case "PASCO":
            furnitureStateString = "Ne convient pas";
            badgeBgClass = "badge-bg-red";
            fetch("/api/visits/" + furnitureData.visitId, {
                method: "GET",
                headers: {
                    Authorization: user.userCo.token,
                },
            })
                .then((response) => {
                    if (!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
                    return response.json();
                })
                .then((data) => {
                    if(data.state == "ANN"){
                        $("#furnitureInfo").append(`
                            <li class=\"list-group-item\">Raison d'annulation: ${data.cancellationNote}</li>
                        `);
                    }
                })
                .catch((err) => onErrorGettingFurniture(err))
            break;
        case "ENRES":
            isRightStateToAddPhoto = true;
            furnitureStateString = "En restauration";
            badgeBgClass = "badge-bg-lightblue";
            if(usersTokenData.isAdmin){
                $("#changeStateForm").append(`<button id="enResButton" type="button" class="btn btn-secondary ml-5 mt-5">Mettre en magasin</button>`);
                $("#enResButton").on("click",submitEnresForm);
                $("#changeStateForm").append(`<button id="SellToAnt" type="button" class="btn btn-secondary ml-5 mt-5">Vendre à un antiquaire</button>`);
                $("#SellToAnt").on("click",displayVenteClient);
            }
            break;
        case "ENMAG":
            isRightStateToAddPhoto = true;
            furnitureStateString = "En magasin";
            badgeBgClass = "badge-bg-marineblue";
            if(usersTokenData.isAdmin){
                $("#changeStateForm").append(`<button id="SellToAnt" type="button" class="btn btn-secondary ml-5 mt-5">Vendre à un antiquaire</button>`);
                $("#SellToAnt").on("click",displayVenteClient);
                $("#changeStateForm").append(`<button id="enMagButton" type="button" class="btn btn-secondary ml-5 mt-5">Mettre en vente</button>`);
                $("#enMagButton").on("click",displayEnMagForm);
            }
            break;
        case "ENVEN":
            isRightStateToAddPhoto = true;
            furnitureStateString = "En vente";
            badgeBgClass = "badge-bg-green";
            if (usersTokenData.isAdmin){
                $("#changeStateForm").append(`<button id="envenButton" type="button" class="btn btn-secondary ml-5 mt-5">Retirer de la vente</button>`);
                $("#envenButton").on("click",withrawalFromSale);

                $("#changeStateForm").append(`<button id="venduButton" type="button" class="btn btn-secondary ml-5 mt-5">vendre le meuble</button>`);
                $("#venduButton").on("click", sell);
            }
            
            $("#changeStateForm").append(`<button id="envenToEnoptButton" type="button" class="btn btn-secondary ml-5 mt-5">Introduire une option</button>`);
            $("#envenToEnoptButton").on("click",displayEnVenForm);
            break;
        case "ENOPT":
            isRightStateToAddPhoto = true;
            furnitureStateString = "En option";
            badgeBgClass = "badge-bg-darkblue";
            if (usersTokenData.isAdmin){
                $("#changeStateForm").append(`<button id="removeOptionButton" type="button" class="btn btn-secondary ml-5 mt-5">Annuler l'option</button>`);
                $("#removeOptionButton").on("click",removeOption);

                $("#changeStateForm").append(`<button id="venduButton" type="button" class="btn btn-secondary ml-5 mt-5">vendre le meuble</button>`);
                $("#venduButton").on("click", sell);
            }
            break;
        case "RESER":
            isRightStateToAddPhoto = true;
            furnitureStateString = "Réservé";
            badgeBgClass = "badge-bg-darkgreen";
            break;
        case "VENDU":
            isRightStateToAddPhoto = true;
            furnitureStateString = "Vendu";
            badgeBgClass = "badge-bg-orange";
            break;
        case "LIVRE":
            furnitureStateString = "Livré";
            badgeBgClass = "badge-bg-lightgreen";
            break;
        case "EMPOR":
            furnitureStateString = "Emporté";
            badgeBgClass = "badge-bg-purple";
            break;
        case "RETIR":
            furnitureStateString = "Retiré";
            badgeBgClass = "badge-bg-darkred";
            break;
    }

    $("#main").prepend(`
    <div class="badge ${badgeBgClass} mt-5 ml-5 mb-2" id="furniture-state-badge">${furnitureStateString}</div>
    <div id="furniture-content">
        <div id="carousel" class="row mr-0"></div>`);
    
    if (furnitureData.photos.length > 1) {
        $("#carousel").prepend(`
        <div class="pt-5 pb-5 col-7">
            <div id="page-furniture-carousel">
                <div class="row d-flex justify-content-center">
                    <div class="col-7 d-flex justify-content-center">
                        <a class="btn mb-3" href="#indicators" role="button" data-slide="prev">
                            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                            <span class="sr-only">Previous</span>
                        </a>
                        <a class="btn mb-3" href="#indicators" role="button" data-slide="next">
                            <span class="carousel-control-next-icon" aria-hidden="true"></span>
                            <span class="sr-only">Next</span>
                        </a>
                    </div>
                </div>
                <div class="row d-flex justify-content-center">
                    <div class="col-7">
                        <div id="indicators" class="carousel slide" data-ride="carousel" data-interval="false">
                        <div class="carousel-inner"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        `);
        let i = 0;
        let j;
        let filledHeart = "<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"bi bi-heart-fill heart-icon\" viewBox=\"0 0 16 16\"><path fill-rule=\"evenodd\" d=\"M8 1.314C12.438-3.248 23.534 4.735 8 15-7.534 4.736 3.562-3.248 8 1.314z\"/></svg>";
        let emptyHeart = "<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"bi bi-heart heart-icon\" viewBox=\"0 0 16 16\"><path d=\"m8 2.748-.717-.737C5.6.281 2.514.878 1.4 3.053c-.523 1.023-.641 2.5.314 4.385.92 1.815 2.834 3.989 6.286 6.357 3.452-2.368 5.365-4.542 6.286-6.357.955-1.886.838-3.362.314-4.385C13.486.878 10.4.28 8.717 2.01L8 2.748zM8 15C-7.333 4.868 3.279-3.04 7.824 1.143c.06.055.119.112.176.171a3.12 3.12 0 0 1 .176-.17C12.72-3.042 23.333 4.867 8 15z\"/></svg>";
        if(usersTokenData.isAdmin){
            let btnScrollableTxt = "Rendre défilable";
            let heartIcon = filledHeart;
            $("#page-furniture-carousel .carousel-inner").empty()
            furnitureData.photos.forEach(photo => {
                if (i % 2 == 0) {
                    j = i;
                    $("#page-furniture-carousel .carousel-inner").append(`<div id="page-furniture-carousel-item${i}" class="carousel-item">
                    <div class="row"></div>
                    </div>`);
                    $("#page-furniture-carousel-item0").addClass("active")
                }
                if (photo.id == furnitureData.favouritePhoto.id) {
                    heartIcon = filledHeart
                } else {
                    heartIcon = emptyHeart;
                }
                if(photo.scrollable){
                    btnScrollableTxt = "Rendre non défilable";
                } else {
                    btnScrollableTxt = "Rendre défilable";
                }
                $(`#page-furniture-carousel-item${j} .row`).append(`
                    <div class="col-lg-6 mb-3">
                        <div class="card">
                            <div class="d-flex justify-content-between furniture-buttons">
                                <button type="button"  id="btn-scrollable${i}" data-id="${photo.id}" data-scrollable="${photo.scrollable}" class="btn btn-secondary btn-sm btn-scrollable">
                                    ${btnScrollableTxt}
                                </button>
                                <button type="button"  id="btn-favorite${i}" class="btn btn-outline-danger btn-sm">
                                    ${heartIcon}
                                </button>
                            </div>
                            <img class="img-fluid" alt="image du meuble" src="data:image/${photo.extension};base64,${photo.bytes}">
                        </div>
                    </div>
                `);
                $("#btn-scrollable"+i).on("click", onChangingScrollable);
                if (photo.id != furnitureData.favouritePhoto.id) {
                    $("#btn-favorite"+i).on("click", (e) => onChangingFavouritePhoto(e,furnitureData.id,photo.id));
                }
                i++;
            })
        } else {
            $("#page-furniture-carousel .carousel-inner").empty()
            furnitureData.photos.forEach(photo => {
                if(photo.scrollable){
                    if (i % 2 == 0) {
                        j = i;
                        $("#page-furniture-carousel .carousel-inner").append(`<div id="page-furniture-carousel-item${i}" class="carousel-item">
                        <div class="row"></div>
                        </div>`);
                        $("#page-furniture-carousel-item0").addClass("active")
                    }
                    $(`#page-furniture-carousel-item${j} .row`).append(`
                        <div class="col-lg-6 mb-3">
                            <div class="card">
                                <img class="img-fluid" alt="image du meuble" src="data:image/${photo.extension};base64,${photo.bytes}">
                            </div>
                        </div>
                    `);
                    i++;
                }
            })
        }

    } else {
        $("#carousel").prepend(`
        <div class="col-6 d-flex justify-content-center">
            <img id="image-furniture" src="data:image/${data.favouritePhoto.extension};base64,${data.favouritePhoto.bytes}"></img>
        </div>
        
        `)
    }
    $("#carousel").append(`
    <div id="furnitureInfo-wraper" class="col-5 d-flex justify-content-start">
        <div class="row">
            <ul class="list-group list-group-flush" id="furnitureInfo">
                <li class="list-group-item">Type : ${data.type}</li>
                <li class="list-group-item">Description : ${data.description}</li>
            </ul>
        </div>
    </div>
    `)
    

    //Displays purchase price if it's not null
    if(data.purchasePrice != -1 && usersTokenData.isAdmin){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\">Prix d'achat : ${data.purchasePrice} </li>
        `);
    }

    //Displays dateCarryFromClient if it's not null
    if(data.dateCarryFromClient != null){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\">Date d'emport : ${convertToLocalDate(data.dateCarryFromClient)} </li>
        `);
    }

    //Displays date Carry to store if it's not null
    if(data.dateCarryToStore != null){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\">Date de dépôt en magasin : ${convertToLocalDate(data.dateCarryToStore)} </li>
        `);
    }

     //Displays sell price if it's not null
     if(data.sellPrice != -1){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\">prix de vente : ${data.sellPrice} </li>
        `);
    }

    //Displays special price if it's not null
    if(data.specialPrice != -1 && (usersTokenData.isAdmin || usersTokenData.isAntiquarian)){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\">prix spécial (antiquaires) : ${data.specialPrice} </li>
        `);
    }

    //Displays purchaser's username if it's not null
    if(data.purchaser && usersTokenData.isAdmin){
        $("#furnitureInfo").append(`
                <li class=\"list-group-item\" id="purchaser">
                    Acheteur: 
                </li> 
        `);
        $("#purchaser").on("click", onNavigate)
        findUser(data.purchaser.id,true);
    }

    if(data.seller && usersTokenData.isAdmin){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\" id="seller">vendeur: </li>
            
        `);
        findUser(data.seller.id,false);
    }
    
    if(data.state =="ENOPT" && usersTokenData.isAdmin){
        $("#furnitureInfo").append(`
            <li class=\"list-group-item\" id="optionInf"></li>
            
        `);
        findOption(data.id);
    }


    if (usersTokenData.isAdmin && isRightStateToAddPhoto){
        $("#changeStateForm").append(`
            <button id="addPhoto" type="button" class="btn btn-secondary ml-5 mt-5">Ajouter une photo</button>
        `);
        $("#addPhoto").on("click", displayFormAddPhoto);
    }

}

function findUser(id,isPurchaser){
    fetch("/api/users/"+ id, {
        method: "GET",
        headers:{Authorization: user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => displayUsername(data, isPurchaser))
    .catch((err) => displayErrorOnErrorPage(err))
}

function displayUsername(data, isPurchaser){
    if(isPurchaser) $("#purchaser").append(`${data.userName}`);
    else $("#seller").append(data.userName);
    
}

/**
 * Displays an error in the page when getting furniture datas
 * @param {} err Error, if it contains a message, it will be shown.
 */
function onErrorGettingFurniture(err) {
    $("#err").remove();
    if (err.message) $("#main").append(`<p id="err" class="alert alert-danger mt-5">${err.message}</p>`)
    else $("#main").append(`<p id="err" class="alert alert-danger">Il y a eu un probléme lors de la récupération du meuble</p>`)
}

/**
 * display the form for propo state 
 */
function displayPropoForm(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`
    <div id= "formPropFrame">
        <h3>Changer d'état le meuble: </h3>
        <form id="propForm">
            <div>
                <input type="radio" id="PROPOENRES" name="radioPROPO" value="ENRES" checked>
                en restauration
            </div>
            <div>
                <input type="radio" id="PROPOENMAG" name="radioPROPO" value="ENMAG">
                en magasin
            </div>
            Prix d'achat : <input type="number" step=".01" class="form-control mt-1" id="propForm-PurchasePrice" required>
            Date d'emport prévu(depuis chez le client): <input type="date" class="form-control mt-1" id="propForm-dateCarryFromClient" required>

            <input type="submit" class="form-control mt-2" id="propForm-submit" value="modifier">

        </form>
    </div>    
    `);

    $("#propForm").on("submit",submitPropForm);
}


/**
 * submit form to change state enProp -> enRes or enMag 
 */
function submitPropForm(e){
    e.preventDefault();
    let furniture = {
        purchasePrice : $("#propForm-PurchasePrice").val(),
        id : furnitureData.id,
        dateCarryFromClient : convert($("#propForm-dateCarryFromClient").val())
    }

    let radioChoice = $("input[name='radioPROPO']:checked").val();
    

    fetch("/api/furnitures/fixPurchasePrice/?nextState=" + radioChoice, {
        method : "POST",
        body : JSON.stringify(furniture),
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err)) 
}

/**
 * submit form to change state enRes -> enMag 
 */
function submitEnresForm(e){
    e.preventDefault();

    fetch("/api/furnitures/carryToStore/" + furnitureData.id, {
        method : "POST",
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err)) 
}

/**
 * display the form for enVen state 
 */
function displayEnMagForm(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`
    <div id= "formEnMagFrame">
        <h3>Changer d'état le meuble: </h3>
        <form id="enMagForm">
            Prix de vente: <input type="number" step=".01" class="form-control mt-1" id="enMagForm-SellPrice" required>
            <input type="submit" class="form-control mt-2" id="enMagForm-submit" value="modifier">
        </form>
    </div>
    `);
    $("#enMagForm").on("submit",submitEnMagForm);
}


/**
 * submit form to change state enRes -> enMag 
 */
function submitEnMagForm(e){
    e.preventDefault();
    let furniture = {
        sellPrice : $("#enMagForm-SellPrice").val(),
        specialPrice : -1,
        id : furnitureData.id
    }

    fetch("/api/furnitures/onSale/?nextState=ENVEN", {
        method : "POST",
        body : JSON.stringify(furniture),
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err)) 
}

function onChangeState(data){
    furnitureData = data;
    getFurnitureData();
}

function withrawalFromSale() {
    let id = findGetParameter("idMeuble");
    fetch("/api/furnitures/withdrawalFromSale/" + id, {
        method : "POST",
        headers : {Authorization : user.userCo.token}
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => getFurnitureData())
    .catch((err) => displayErrorOnThisPage(err));
}

/**
 * display the form for enVen state 
 */
 function displayEnVenForm(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`
    <div id= "formEnVenFrame">
        <h3>Changer d'état le meuble: </h3>
        <form id="enVenForm">
        <div class="col-auto">
            <select class="form-select btn btn-secondary dropdown-toggle" id="duration" >
                <option selected>Nombre de jours de l'option</option>
                <option value="1">1 jour</option>
                <option value="2">2 jours</option>
                <option value="3">3 jours</option>
                <option value="4">4 jours</option>
                <option value="5">5 jours</option>
            </select>
        </div>

            <input type="submit" class="form-control mt-2" id="enVenForm-submit" value="Confirmer">
        </form>
    </div>
    `);
    $("#enVenForm").on("submit",submitEnVenForm);
}

/**
 * submit form to change state enRes -> enMag 
 */
 function submitEnVenForm(e){
    user = getUserStorageData().userCo;
    e.preventDefault();
     fetch("/api/furnitures/getOption/"+user.user.id+"/"+furnitureData.id,{
         method: "GET",
         headers : {
             Authorization : user.token
         }
     })
     .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => createAnOption(data))
    .catch((err) => displayErrorOnThisPage(err))
   
    
}
function createAnOption(data){
    if(data.isCancel){
        let option = {
            furnitureId : furnitureData.id,
            duration : $("#duration").val(),
            isCancel : true,
            daysLeft:data.daysLeft
        }
        fetch("/api/furnitures/createOption", {
            method : "POST",
            body : JSON.stringify(option),
            headers : {
                "Content-Type" : "application/json",
                Authorization : getUserStorageData().userCo.token
            }
        })
        .then((response) => {
            if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
            return response.json();
        })
        .then(() => getFurnitureData())
        .catch((err) => displayErrorOnThisPage(err)) 

    }
    else{
        let option = {
            furnitureId : furnitureData.id,
            duration : $("#duration").val(),
            isCancel : false,
            daysLeft:5
        }
    
        fetch("/api/furnitures/createOption", {
            method : "POST",
            body : JSON.stringify(option),
            headers : {
                "Content-Type" : "application/json",
                Authorization : getUserStorageData().userCo.token
            }
        })
        .then((response) => {
            if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
            return response.json();
        })
        .then(() => getFurnitureData())
        .catch((err) => displayErrorOnThisPage(err)) 
    }
}

function removeOption(e) {
    e.preventDefault()
    fetch("/api/furnitures/removeOption/" + furnitureData.id, {
        method : "PUT",
        headers : {"Content-Type" : "application/json", Authorization : user.userCo.token}
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => displayFurniture())
    .catch((err) => displayErrorOnThisPage(err))
}

function displayFormAddPhoto(e) {
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`
    <div id="formAddPhoto">
        <h3>Ajouter une photo au meuble: </h3>
        <form id="addPhotoForm" enctype="multipart/form-data">
            <div class="col-auto">
               <div class="form-group">
                    <label for="photo">Photo :</label>
                    <input type="file" class="form-control" id="photo" accept="image/*">
                </div>
            </div>
            <input type="submit" class="form-control mt-2" id="enVenForm-submit" value="Confirmer">
        </form>
    </div>
    `);
    $("#addPhotoForm").on("submit",submitAddPhoto);
}

function submitAddPhoto(e) {
    e.preventDefault()
    const formData = new FormData()
    formData.append('file', new Blob($("#photo").prop('files')), $("#photo").prop('files')[0].name)
    formData.append('furniture', JSON.stringify({sellPrice : 90.4}))
    fetch("/api/furnitures/addPhoto/"+furnitureData.id, {
        method : "POST",
        body : formData,
        headers : {
            Authorization : user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw Error("Erreur: " + response.status + " : " + response.statusText) 
        displayFurniture()
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function sell(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`<button id="VenteEnMag" type="button" class="btn btn-secondary ml-5 mt-5">vente sans compte</button>`);
    $("#VenteEnMag").on("click", displayVenteEnMag);

    $("#changeStateForm").append(`<button id="venteAUnClient" type="button" class="btn btn-secondary ml-5 mt-5">vente avec compte client</button>`);
    $("#venteAUnClient").on("click", displayVenteClient);
}

function displayVenteEnMag(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    $("#changeStateForm").append(`
    <div id="formVenduFrame">
        <h3>Vendre le meuble: </h3>
        <form id="formVendu">
            Prix spécial pour antiquaire (laisser vide si non-antiquaire): 
            <input type="number" step=".01" class="form-control mt-1" id="venduForm-specialPrice">
            <input type="submit" class="form-control mt-2" id="venduForm-submit" value="Vendre">
        </form>
    </div>
    `);
    $("#formVendu").on("submit",sendSellForm)
}

function displayVenteClient(e){
    e.preventDefault();
    $("#changeStateForm").empty();
    let antObligatoire = false;
    if(furnitureData.state == "ENVEN" || furnitureData.state == "ENOPT"){
        $("#changeStateForm").append(`
    <div id="SaleWithClientFormFrame">
            1.Rechercher le client voulant acheter le meuble </br>
            <form id="researchSale">   
                <input type="text" placeholder="Acheteur"  class="form-control mt-1" id="research-inputOnSaleForm">
                <input id="submit-researchClient"  class="form-control mt-2" type="submit" value="valider">
            </form>
            
            <form id="SaleWithClient">
                <div id="radioChoice"></div>
                2.Indiquer un prix spécial si il s'agit d'un antiquaire </br>
                <input type="number" step=".01" class="form-control mt-1" id="venduForm-specialPrice">    
                <input type="submit" class="form-control mt-2" id="venduForm-submit" value="Vendre">
            </form>
    </div>
    
    `);
    }else if(furnitureData.state == "ENRES" || furnitureData.state == "ENMAG"){
        antObligatoire = true;
        $("#changeStateForm").append(`
    <div id="SaleWithClientFormFrame">
            1.Rechercher l'antiquaire voulant acheter le meuble </br>
            <form id="researchSale">   
                <input type="text" placeholder="Acheteur"  class="form-control mt-1" id="research-inputOnSaleForm">
                <input id="submit-researchClient"  class="form-control mt-2" type="submit" value="valider">
            </form>
            
            <form id="SaleWithClient">
                <div id="radioChoice"></div>
                2.Indiquer un prix spécial (obligatoire) </br>
                <input type="number" step=".01" class="form-control mt-1" id="venduForm-specialPrice" required>    
                <input type="submit" class="form-control mt-2" id="venduForm-submit" value="Vendre">
            </form>
    </div>
    
    `);
    }
    
    $("#researchSale").on("submit",(e) => researchPurchaser(e,antObligatoire));
    $("#SaleWithClient").on("submit",sendSellFormWithClient);
}

function sendSellForm(e){

    e.preventDefault();

    let specialPriceInput = -1;
    if ($("#venduForm-specialPrice").val() != "") specialPriceInput = $("#venduForm-specialPrice").val();
    
    let furniture = {
        id : furnitureData.id,
        state : furnitureData.state,
        specialPrice : specialPriceInput
    }

    fetch("/api/furnitures/sellFurniture/", {
        method : "POST",
        body : JSON.stringify(furniture),
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err)) 
}

function researchPurchaser(e, antObligatoire){
    e.preventDefault();
    fetch("api/users/research?word=" + $("#research-inputOnSaleForm").val(),{
        method: "GET",
        headers: {
            Authorization: user.userCo.token,
        },
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => {
        if(antObligatoire){
            let antData = [];
            data.forEach(client => {
                if(client.role == "ANT") antData.push(client);
            }); 
            data = antData;
        }
        displayRadioPurchaser(data)
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function displayRadioPurchaser(data){
    $("#radioChoice").empty();
    if(data.join() == [].join()){
        $("#radioChoice").append(`
        <div class="inputResearch">
            Pas de client correspondant à la recherche.
        </div>       
    `);
    }   
    else{
        data.forEach(client => {
            $("#radioChoice").append(`
            <div class="inputResearch">
                <input type="radio" id="client${client.id}" class="inputResearch" name="radioClient" value="${client.id},${client.role}" checked>
                ${client.userName}
            </div>       
            `);
        })
    }
    
}

function sendSellFormWithClient(e){
    e.preventDefault();

    let specialPriceInput = -1;
    if ($("#venduForm-specialPrice").val() != "") specialPriceInput = $("#venduForm-specialPrice").val();
    let infoUserSearched = $("input[name='radioClient']:checked").val().split(",");
    let furniture = {
        id : furnitureData.id,
        state : furnitureData.state,
        specialPrice : specialPriceInput,
        purchaser: {
            id : infoUserSearched[0],
            role : infoUserSearched[1]
        }
    }

    fetch("/api/furnitures/sellFurniture/", {
        method : "POST",
        body : JSON.stringify(furniture),
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err)) 

}

function onChangingScrollable(e){
    e.preventDefault();
    fetch("/api/furnitures/changeScrollable/" + e.target.dataset.id + "/" + e.target.dataset.scrollable, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        displayFurniture();
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function onChangingFavouritePhoto(e, idFurniture, idPhoto){
    e.preventDefault();

    fetch("/api/furnitures/changeFavouritePhoto/" + idFurniture + "/" + idPhoto, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Authorization: user.userCo.token
        }
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        displayFurniture();
    })
    .catch((err) => displayErrorOnThisPage(err))
}

function findOption(id){
    fetch("/api/furnitures/getActualOption/" + id, {
        method: "GET",
        headers: {
            Authorization: user.userCo.token,
        },
    })
        .then((response) => {
            if (!response.ok) throw new Error("Code d'erreur : " + response.status + " : " + response.statusText);
            return response.json();
        })
        .then((data) => getOptioner(data))
        .catch((err) => onErrorGettingFurniture(err))
}

function getOptioner(o){
    fetch("/api/users/"+ o.userId, {
        method: "GET",
        headers:{Authorization: user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => displayOption(o, data))
    .catch((err) => displayErrorOnErrorPage(err))
}

function displayOption(o, optioner){
    $("#optionInf").append(`Option de ${optioner.userName} jusqu'au ${convertToLocalDate(o.limitDate)}`);
}

function sendNotSuitable(e){
    e.preventDefault()
    fetch("/api/furnitures/notSuitable", {
        method : "POST",
        body : JSON.stringify(furnitureData),
        headers : {
            "Content-Type" : "application/json",
            Authorization : getUserStorageData().userCo.token
        }
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onChangeState(data))
    .catch((err) => displayErrorOnThisPage(err))
}

export default displayFurniture;
