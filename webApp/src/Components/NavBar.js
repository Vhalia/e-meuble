import { onNavigate, redirectUrl} from "./Router";
import {getUserStorageData} from "../Utils/storage";
import {onDisconnection,onDisplayFurnitures} from "./Home";
import logo from "../img/logoAE_v2.png";
import LiViSatcho from "../img/LiViSatcho.png";
import {displayErrorOnThisPage} from "./Error.js";
const jwt = require("jsonwebtoken")

let tags = [];
let autocomplete = false;
let d;
let infoUser = 0;

function displayNavBar(){
    $("#navbar").empty()
    $("#navbar").append(`
    <div id="highNav"> 
        <div id= "divLogo">
            <img id = "logo" src="${logo}" alt="logo" data-url ="/" height="150px" width="150px"/>
            <p class="text-white ml-4">1bis sente des artistes – </br>
            Verviers</p>
        </div>    
        <div id= "nom">
            <img src="${LiViSatcho}" alt="name" height="150px" width="550px"/> 
        </div>
    </div>
    
    <div id="lowNav" class="d-flex justify-content-between"></div>
    `)
    $("#logo").on('click', onNavigate);
    $('#logo').on("mouseover", () => $("#logo").css("opacity", "0.5"));
    $('#logo').on("mouseleave", () => $("#logo").css("opacity", "1"));
    $('#logo').css('cursor','pointer');
    
    let dataReturned = getUserStorageData();
    fetch("api/furnitures/tags",{
        method: "GET",
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => {
        if(data.join() != [].join()) {
            d = data;
            if(dataReturned){
                infoUser = jwt.decode(dataReturned.userCo.token)
                displayResearch(data,dataReturned);
            }
            
        }
    })
    .catch((err) => displayErrorOnThisPage(err))

    if(!dataReturned){    
        let filterVisible = false; 
        $("#highNav").append(`
        <div>
            <button id="btn-connection" class="btn btn-primary mt-2 ml-2" data-url ="/login" type="button">Se connecter</button>
            <button id="btn-register" class="btn btn-primary mt-2 ml-2" data-url ="/register" type="button">S'enregistrer</button>
        </div>`)
        $("#lowNav").append(`
        <div id="buttonsNavbarLeft">
            <button type="button" class="btn btn-primary" id="filterButton">Filtre</button>
        </div>

        
        `);
        $("#filterButton").on("click",function(){
            displayFilterQuidam(d,filterVisible)
            filterVisible = !filterVisible;
        });
        $("#btn-connection").on("click",onNavigate)
        $("#btn-register").on("click",onNavigate)
    }
    else{
        $("#highNav").append(`
        <div id="dropdown-menu" class="btn-group dropleft">
            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                Menu
            </button>
            <div class="dropdown-menu">
                <a id="profil" data-url ="/profil" class="dropdown-item" >Profil</a>
                <a class="dropdown-item" data-url ="/" id="deconnexion" >Se deconnecter</a>
            </div>
        </div>
        `)
        $("#buttonsNavbarLeft").prepend(`
        <button data-url="/visitRequest" id="requestVisitBtn" class="btn btn-primary">Demander une visite</button>
        `)
        
        const infoUser = jwt.decode(dataReturned.userCo.token)
        if(infoUser.isAdmin) {
            $("#profil").after(`<a class="dropdown-item" data-url ="/admin" id="admin">Admin</a>`)
        }
        $("#deconnexion").on("click", onDisconnection)
        $("#admin").on("click", onNavigate)
        $("#profil").on("click", onNavigate)
        $("#requestVisitBtn").on("click", onNavigate)        
    }
    
}
function displayResearch(data,dataReturned){
    let filterVisible = false;
    $("#lowNav").empty();
    if(infoUser.isAdmin) {  
        $("#lowNav").prepend(`
        <form id = "research-form" data-url="/research" data-keyword=""> 
            <input type="text" placeholder="rechercher" id="research-input">
            <input id="submit-research" type="submit" value="🔎">
        </form>`);
    }

    $("#lowNav").append(`
    <div id="buttonsNavbarLeft">
        <button type="button" class="btn btn-primary" id="filterButton">Filtre</button>
    </div>
    `);

    $("#buttonsNavbarLeft").prepend(`
    <button data-url="/visitRequest" id="requestVisitBtn" class="btn btn-primary">Demander une visite</button>
    `)

    
    $("#requestVisitBtn").on("click", onNavigate)
    
    if(autocomplete == false) chargeTags(dataReturned);
    else{
        $("#research-input").autocomplete({source: tags});
       
    }
    $("#research-form").on("submit", (e) => {
        e.target.dataset.keyword = $("#research-input").val()
        onNavigate(e);
    });
    $("#filterButton").on("click",function(){
        displayFilter(dataReturned,data,filterVisible)
        filterVisible = !filterVisible;
    });
}
function chargeTags(dataReturned){
    fetch("api/furnitures/tags/",{
        method: "GET",
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => addTags(data))
    .catch((err) => displayErrorOnThisPage(err)) 
    const decodedToken = jwt.decode(dataReturned.userCo.token)
    if (decodedToken.isAdmin) {
        fetch("api/users/tags/",{
            method: "GET",
            headers: {
                Authorization: dataReturned.userCo.token,
            },
        })
        .then((response) => {
            if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
            return response.json();
        })
        .then((data) => addTags(data))
        .catch((err) => displayErrorOnThisPage(err)) 
    }
    
}

function addTags(data){
    tags = tags.concat(data);
    if(autocomplete == true)
        $("#research-input").autocomplete({source: tags});
    else autocomplete = true;    
}

function onSubmitFilter(e,dataReturned){
    e.preventDefault();
    let minVal =  $("#minPrice").val();
    let maxVal =  $("#maxPrice").val();
    let type = $(`input[name="type"]:checked`).val();
    
    if(maxVal == 0){
        maxVal = 1000000000;
    }
    if(type == undefined){
        type = 0;
    }
    fetch("/api/furnitures/FiltredFurnitures/"+minVal+"/"+maxVal+"/" + type, {
        method : "GET",
        headers: {
            Authorization: dataReturned.userCo.token,
        },
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onDisplayFurnitures(data))
    .catch((err) => displayErrorOnThisPage(err))
}


function onSubmitFilterQuidam(e){
    e.preventDefault();
    let type = $(`input[name="type"]:checked`).val();
    
    if(type == undefined){
        type = 0;
    }
    fetch("/api/furnitures/FiltredFurnitures/"+ type, {
        method : "GET",
    })
    .then((response) => {
        if(!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onDisplayFurnitures(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function displayFilter(dataReturned,data,filterVisible){
    if(filterVisible){
        displayResearch(dataReturned,data);
    }
    else{
        $("#lowNav").append(`    
        <form id="filter-form">
            <div id="dropdownType" class="radio-dropdown">
                Type
                <ul class="radio-dropdown-list" id="tabType">
                    <li>
                        <label><input type="radio" value=${0} name="type"/> Tous les types</label>
                    </li>
                </ul>
            </div>

            <div id="blocInputPrice">
                Entre 
                <input type="number" step=".01" id="minPrice" class="input-price form-control" value="0"/> et
                <input type="number" step=".01" id="maxPrice" class="input-price form-control" value="0"/> €
            </div>
            <input type="submit" id= "submitFilter"  class="btn btn-primary" value="Appliquer">
        </form>     
        `);
        for(let i=0;i<data.length;i++){
            $("#tabType").append(`
            <li>
                <label><input type="radio" value=${i+1} name="type"/>${data[i]}</label>
            </li>
            `);
        }
        $("#filter-form").on("submit", (e) =>onSubmitFilter(e,dataReturned));

            //dropdown invisible
        $(".radio-dropdown").on("click",function () {
            $(this).toggleClass("is-active");
        });

        $(".radio-dropdown ul").on("click",function(e) {
            e.stopPropagation();
        });
    }
}


function displayFilterQuidam(data,filterVisible){
    if(filterVisible){
        displayNavBar();
    }
    else{
        $("#lowNav").append(`    
        <form id="filter-form">
            <div class="radio-dropdown">
                Type
                <ul class="radio-dropdown-list" id="tabType">
                    <li>
                        <label><input type="radio" value=${0} name="type"/> Tous les types</label>
                    </li>
                </ul>
            </div>
            <input class="btn btn-primary" type="submit" id="submitFilter" value="Confirmer">
        </form>     
        `);
        for(let i=0;i<data.length;i++){
            $("#tabType").append(`
            <li>
                <label><input type="radio" value=${i+1} name="type"/>${data[i]}</label>
            </li>
            `);
        }

        $("#filter-form").on("submit", (e) =>onSubmitFilterQuidam(e));

            //dropdown invisible
        $(".radio-dropdown").on("click",function () {
            $(this).toggleClass("is-active");
        });

        $(".radio-dropdown ul").on("click",function(e) {
            e.stopPropagation();
        });
    }
}
export default displayNavBar;