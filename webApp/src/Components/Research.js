import findGetParameter from "../Utils/urlUtil.js";
import displayNavbar from "./NavBar.js";
import { getUserStorageData } from "../Utils/storage.js";
import {convertToLocalDate, convert} from "../Utils/dateUtil.js";
import {displayErrorOnThisPage} from "./Error.js";
import { onNavigate} from "./Router";

let keyword;
let user;
let userTokenData;
const jwt = require("jsonwebtoken")
let furnitures = [];

function displayResultResearch(){
    $("#navbar").empty();
    displayNavbar();
    getResearchData();
}

function getResearchData(){
    user = getUserStorageData();
    if(!user){
        throw new Error("Erreur: vous n'etes pas connecté");
    }else{

    }
    userTokenData = jwt.decode(user.userCo.token)

    keyword = findGetParameter("keyword");
    $('#main').empty();

    if(userTokenData.isAdmin) displayAdminResearch();
    else displayClientResearch();
}


function displayAdminResearch(){

    $('#main').append(`
    <h4 id="resultSearchSent"> résultats de recherche pour ${keyword}</h4>
    <div id="clientResearch"></div>
    <div id="furnitureResearch"></div>
    `);

    //clients Tab call
    fetch("api/users/research?word=" + keyword,{
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
        if(data.join() != [].join()) displayClientTab(data)
    })
    .catch((err) => displayErrorOnThisPage(err)) 


    //furnitures Tab call
    fetch("api/furnitures/research?word=" + keyword,{
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
        if(data.join() != [].join()) displayFurnituresTab(data)
    })
    .catch((err) => displayErrorOnThisPage(err)) 



}


function displayClientTab(data){
    $("#clientResearch").append(`
        <table class="table table-striped table-dark container mt-2"> 
            <thead>
                <tr>
                    <th>Pseudo</th>
                    <th>Nom</th>
                    <th>Prénom</th>
                    <th>email</th>
                    <th>Rôle</th>
                    <th>date d'inscription</th>
                    <th>inscription validée</th>
                    <th>nombre de meubles achetés</th>
                    <th>nombre de meubles vendu</th>
                </tr>
            </thead>
            <tbody id=contentTableResearchClient></tbody>
        </table>
    `); 

    data.forEach(client => {
        $("#contentTableResearchClient").append(`
        <tr data-url="/viewProfil" data-id="${client.id}" data-querykey="idUser">
            <td>${client.userName}</td>
            <td>${client.name}</td>
            <td>${client.surname}</td>
            <td>${client.email}</td>
            <td>${client.role}</td>
            <td>${convertToLocalDate(client.registrationDate)}</td>
            <td>${client.registrationValidated}</td>
            <td>${client.nbrFurnituresBought}</td>
            <td>${client.nbrFurnituresSold}</td>
        </tr>
        `);
    });
    $("#contentTableResearchClient tr").on("click", onNavigate)
}

function displayFurnituresTab(data){
    $("#furnitureResearch").append(`
        
        <p id="filtrerResearch">Filtrer les meubles liés à la recherche: <br>
        Afficher les meubles allant de <input id="min" type="number" step=".01"> à <input id="max" type="number" step=".01"> € <button class="btn btn-secondary" id="validateFilter"> confirmer</button></p> 
        <table class="table table-striped table-dark container mt-2"> 
            <thead>
                <tr>
                    <th>Type</th>
                    <th>Etat</th>
                    <th>Prix d'achat</th>
                    <th>Date d'emport depuis le client</th>
                    <th>Date de dépot en magasin</th>
                    <th>Prix de vente</th>
                    <th>Prix spécial</th>
                    <th>Vendeur</th>
                </tr>
            </thead>
            <tbody id=contentTableResearchFurnitures></tbody>
        </table>
    `); 

    furnitures = data; 
    furnitures.forEach(furniture => {
        $("#contentTableResearchFurnitures").append(`
        <tr data-url="/furnitures" data-id="${furniture.id}" data-querykey="idMeuble">
                <td>${furniture.type}</td>   
                <td>${furniture.state}</td>
                <td id="ResTabPP${furniture.id}"></td>
                <td id="ResTabDCC${furniture.id}"></td>
                <td id="ResTabDCS${furniture.id}"></td>
                <td id="ResTabSP${furniture.id}"></td>
                <td id="ResTabSSP${furniture.id}"></td>
                <td id="ResTabSeller${furniture.id}"></td>  
        </tr>
        `);
        if(furniture.purchaseprice != -1) $("#ResTabPP" + furniture.id).append(`${furniture.purchasePrice}`);
        if(furniture.sellPrice != -1) $("#ResTabSP" + furniture.id).append(`${furniture.sellPrice}`);
        if(furniture.specialPrice != -1) $("#ResTabSSP" + furniture.id).append(`${furniture.specialPrice}`);
        if(furniture.dateCarryFromClient != null) $("#ResTabDCC" + furniture.id).append(`${convertToLocalDate(furniture.dateCarryFromClient)}`);
        if(furniture.dateCarryToStore != null) $("#ResTabDCS" + furniture.id).append(`${convertToLocalDate(furniture.dateCarryToStore)}`);
        if(furniture.seller != null) getSeller(furniture.seller.id, furniture.id)

    });

    $("#contentTableResearchFurnitures tr").on("click", onNavigate)
    $("#validateFilter").on("click", fiterFurnitures)

}

function getSeller(id, fid){
    fetch("/api/users/"+ id, {
        method: "GET",
        headers:{Authorization: user.userCo.token}
    })
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
        return response.json()
    })
    .then((data) => $("#ResTabSeller" + fid).append(`${data.userName}`))
    .catch((err) => displayErrorOnErrorPage(err))
}

function fiterFurnitures(e){
    e.preventDefault();

    let valeurMin = 0;
    let valeurMax = 0;
    if ($("#min").val() != "") valeurMin = $("#min").val();
    if ($("#max").val() != "") valeurMax = $("#max").val();
    
    let furnitures2 = [];

    furnitures.forEach(
        furniture =>{
            if(furniture.sellPrice>= valeurMin && furniture.sellPrice <= valeurMax) furnitures2.push(furniture);
        }
    );
    furnitures = furnitures2;
    $("#furnitureResearch").empty();
    displayFurnituresTab(furnitures);

}

export default displayResultResearch;