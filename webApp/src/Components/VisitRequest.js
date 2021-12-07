import verifyType from "../Utils/fileUtil"
import { getUserStorageData } from "../Utils/storage"
import { displayErrorOnThisPage } from "./Error"
import displayNavBar from "./NavBar"
import { redirectUrl } from "./Router"
const jwt = require("jsonwebtoken")

let furnitures = [];
let photosOfAFurniture = [];

let user;
let formdata;

function displayVisitRequest() {
    furnitures = [];
    photosOfAFurniture = [];
    formdata = new FormData();
    user = getUserStorageData()
    $("#main").empty()
    displayNavBar()
    $("#main").append(`
    <div class="col-12">
        <div id="headerVisitRequest" class="row justify-content-center mt-5">
            <div>
                <h1>Demande de visite</h1>
                <hr class="hr-underline-title">
            </div>
        </div>
        <div class="row m-5 justify-content-between">
            <div class="col-4">
                <h2 class="row text-white mt-5 mb-1">Plage horaire des disponibilités:</h2>
                <textarea rows="5" class="form-control row" id="formDisponibilities" required></textarea >
            </div>
            <div id="addFurniture" class="col-4">
                <h2 class="row text-white justify-content-start mb-5">Ajouter un meuble:</h2>
                <h4 class="row text-white">Ajouter une photo:</h4>
                <div class="row">
                    <input type="file" class="form-control w-50" id="photo" accept="image/*">
                    <button id="btnAddPhoto" class="btn btn-secondary ml-5" disabled>Ajouter une photo</button>
                </div>
                <h4 class="row text-white">Donner une description au meuble:</h4>
                <div class="row">
                    <textarea id="formDescription" class="form-control w-75" rows="3"></textarea>
                </div>
                <h4 class="row text-white">Selectionner le type du meuble:</h4>
                <div class="row">
                    <select id="formSelectType" class="form-control w-50" disabled>
                    </select>
                    <button id="btnAddFurniture" class="btn btn-primary ml-5" disabled>Ajouter un meuble</button>
                </div>
                <p id="infoNbFurnitures" class="alert alert-info row mt-5" hidden></p>
            </div>
            <div id="adrPart" class="col-4">
                <h3 id="titreAdrStorage" class="row text-white mb-1">Adresse du lieu d'entreposage des meubles (Uniquement si différente de votre adresse):</h3>
                <div class="row">
                    <input id="formStreet" type="text" class="form-control w-50 mr-1" placeholder="Rue" />
                    <input id="formNumber" type="number" class="form-control w-25" placeholder="Numero" />
                </div>
                <div class="row mt-1">
                    <input id="formBox" type="text" class="form-control w-25 mr-1" placeholder="Boite" />
                    <input id="formPostalCode" type="number" class="form-control w-25" placeholder="Code postal" />
                </div>
                <div class="row mt-1">
                    <input id="formCommune" type="text" class="form-control w-25 mr-1" placeholder="Commune" />
                    <input id="formCountry" type="text" class="form-control w-50" placeholder="Pays" />
                </div>
            </div>
        </div>
        <div class="row m-5 justify-content-end">
            <button id="btnConfirmVisitRequest" class="btn btn-primary">Introduire la demande de visite</button>
        </div>
    </div>
    `)

    getFurnituresTags()

    let decodedToken = jwt.decode(user.userCo.token)
    if (decodedToken.isAdmin) {
        displayEnterPseudoForm()
    }

    $('#photo').on("change",() => {
        $("#err").remove()
        verifyType($("#photo").prop('files')[0], (typeOk) => {
            if (!typeOk) {
                displayErrorOnThisPage(new Error("Mauvais type de fichier. Types acceptés : png, jpg/jpeg"))
            }else {
                $("#btnAddPhoto").removeAttr("disabled");
            }
        })
    });

    $("#formDescription").on("change", () => {
        $("#formSelectType").removeAttr("disabled");
        $("#btnAddFurniture").removeAttr("disabled");
    })

    $("#btnAddPhoto").on("click", onAddingPhotoToFurniture)
    $("#btnConfirmVisitRequest").on("click", onConfirmingVisitRequest)
    $("#btnAddFurniture").on("click", onAddingFurniture)
}

function getFurnituresTags() {
    fetch("api/furnitures/tags/")
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => displayTagList(data))
    .catch((err) => displayErrorOnThisPage(err)) 
}

function displayTagList(data) {
    for(let i = 0; i < data.length; i++) {
        let attr = ""
        if(i == 0) attr = "selected"
        $("#formSelectType").append(`
            <option value="${data[i]}" ${attr}>${data[i]}</option>
        `)
    }
    
}

function displayEnterPseudoForm(e = null) {
    if (e != null) e.preventDefault()
    $("#parentBtnExisitingClient").remove()
    $("#formFakeClient").remove()
    $("#titreAdrStorage").before(`
    <div id="parentBtnCreateClient" class="row justify-content-end mb-5">
        <button id="btnCreateClient" class="btn btn-secondary">Créer un client</button>
    </div>
    <div id="formPseudo" class="row mb-5">
        <input id="formPseudoInput" class="form-control" type="text" placeholder="Pseudo du client">
    </div>
    `)
    formdata.set("createFakeClient", false)
    $("#titreAdrStorage").text("Adresse du lieu d'entreposage des meubles (Uniquement si différente de l'adresse du client)")

    $("#btnCreateClient").on("click", onCreatingClient)
}

function onCreatingClient(e) {
    e.preventDefault()
    $("#parentBtnCreateClient").remove()
    $("#formPseudo").remove()
    $("#titreAdrStorage").before(`
        <div id="parentBtnExisitingClient" class="row justify-content-end mb-5">
            <button id="btnExisitingClient" class="btn btn-secondary">Entrer un pseudo existant</button>
        </div>
        <div id="formFakeClient" class="row">
            <h3 class="row text-white mb-1">Générales:</h3>
            <div class="row mb-5">
                <input id="formName" type="text" class="form-control mb-1" placeholder="Nom">
                <input id="formSurname" type="text" class="form-control mb-1" placeholder="Prenom">
                <input id="formEmail" type="text" class="form-control" placeholder="Email">
            </div>
        </div>
    `)
    formdata.set("createFakeClient", true)
    $("#titreAdrStorage").text("Adresse:")
    $("#btnExisitingClient").on("click", displayEnterPseudoForm)
}


function onAddingPhotoToFurniture(e) {
    e.preventDefault()
    photosOfAFurniture.push($("#photo").prop('files')[0])
    formdata.append("file", $("#photo").prop('files')[0])
    displayConfirmMessage("Photo ajouté !")
}

function onAddingFurniture(e) {
    e.preventDefault()
    let furniture = {
        type : $("#formSelectType").children("option:selected").val(),
        photos : photosOfAFurniture,
        description : $("#formDescription").val()
    }
    furnitures.push(furniture)
    displayConfirmMessage(`Meuble de type ${furniture.type} ajouté`)
    $("#infoNbFurnitures").removeAttr("hidden")
    $("#infoNbFurnitures").text(`${furnitures.length} meuble(s) dans la demande de visite`)
    photosOfAFurniture = []
    $("#formDescription").val("")

    $("#formSelectType").prop("disabled", true);
    $("#btnAddPhoto").prop("disabled", true);
    $("#btnAddFurniture").prop("disabled", true);
}

function onConfirmingVisitRequest(e) {
    e.preventDefault()
    let visitRequest = {
        usersTimeSlot : $("#formDisponibilities").val(),
        storageAddress : {
            street : $("#formStreet").val(),
            nbr : $("#formNumber").val(),
            box : $("#formBox").val(),
            postalCode : $("#formPostalCode").val(),
            commune : $("#formCommune").val(),
            country : $("#formCountry").val(),
        },
        furnitures : furnitures,
        client : {
            name :$("#formName").val(),
            surname : $("#formSurname").val(),
            userName :$("#formPseudoInput").val(),
            email:$("#formEmail").val(),
        }
    }
    formdata.append("visit", JSON.stringify(visitRequest))
    fetch("/api/visits", {
        method : "POST",
        headers : {
            Authorization : user.userCo.token
        },
        body: formdata
    })
    .then((response) => {
        if (!response.ok){
            let msg = response.statusText
            if(response.status == "400") msg = "Une ou plusieurs informations entrées sont invalides"
            if(response.status == "500") msg = "Quelque chose s'est mal passé, vérifiez les informations entrées et recommencez"
            formdata.delete("visit")
            throw new Error("Erreur: " + response.status + " : " + msg)
        } 
        response.json()
    })
    .then((data) => redirectUrl("/"))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayConfirmMessage(message) {
    $("#confirmMsg").remove()
    $("#main").append(`<p id="confirmMsg" class="container alert alert-success" role="alert">${message}</p>`)
    setTimeout(() => {
        $("#confirmMsg").remove()
    }, 5000)
    
}

export default displayVisitRequest