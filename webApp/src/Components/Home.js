import { onNavigate, redirectUrl } from "./Router";
import {getUserStorageData, removeAllDataStorage} from "../Utils/storage";
import displayNavbar from "./NavBar.js";
import furnitureplaceholder from "../img/furniture-placeholder.png";
import { displayErrorOnThisPage } from "./Error";
const jwt = require("jsonwebtoken");


function displayHome() {
    $("#navbar").empty();
    displayNavbar();
    $("#main").empty();
    displayFurnitures()
}

function displayFurnitures() {
    const user = getUserStorageData();
    let usersTokenData;
    if(user) usersTokenData = jwt.decode(user.userCo.token);
    let request = "/api/furnitures"
    let options;
    if(usersTokenData && usersTokenData.isAdmin) {
      request = "/api/furnitures/allFurnituresAdmin"
      options = {Authorization : user.userCo.token}
    }
    fetch(request, {headers : options})
    .then((response) => {
        if (!response.ok) throw new Error("Erreur: "+ response.status + " : " + response.statusText);
        return response.json();
    })
    .then((data) => onDisplayFurnitures(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function onDisplayFurnitures(data) {
  if(data.join() != [].join()){
  $("#main").empty();
    $("#main").append(`
    <div id="furnitures">
    <!--Carousel Wrapper-->
    <div id="furniture-carousel" class="carousel slide carousel-multi-item" data-ride="carousel" data-interval="false">
    
      <!--Fleches-->
      <div id="next-furniture">
          <a class="carousel-control-prev" href="#furniture-carousel" role="button" data-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="sr-only">Previous</span>
          </a>
      </div>
      <div id="prev-furniture">
        <a class="carousel-control-next" href="#furniture-carousel" role="button" data-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="sr-only">Next</span>
        </a>
      </div>
      <!--/Fleches-->
    
      <!--Slides-->
      <div class="carousel-inner" role="listbox">
      </div>
      <!--/.Slides-->
    
    </div>
    <!--/.Carousel Wrapper-->
    </div>`)
    let i = 0;
    let j;
  
    $("#furniture-carousel .carousel-inner").empty()
  
    data.forEach(furniture => {
      if (i % 4 == 0) {
        j = i;
        $("#furniture-carousel .carousel-inner").append(`<div id="furnitureCarouselItem${i}" class="carousel-item">`);
        $("#furnitureCarouselItem0").addClass("active")
      }
      let stateAndBadge = displayStateOfFurniture(furniture.state)
      $(`#furnitureCarouselItem${j}`).append(`
          <div class="container-card" style="float:left">
            <div class="card mb-2">
              <a data-url="/furnitures" data-id="${furniture.id}" data-querykey="idMeuble">
                <img class="card-img-top" src="data:image/${furniture.favouritePhoto.extension};base64,${furniture.favouritePhoto.bytes}" alt="photo du meuble">
                <div class="badge ${stateAndBadge.badge} position-absolute w-50 text-wrap text-white" id="furnitureStateHome">${stateAndBadge.state}</div>
              </a>
              <hr/>
              <div class="card-body">
                <a data-url="/furnitures" data-id="${furniture.id}">
                  <p class="card-text">${furniture.description}</p>
                </a>
              </div>
            </div>
          </div>`)
      i++;
      
    });

    $("#furnitures .carousel-inner a").on("click", onNavigate)
  }
  else{
    $("#main").empty();
    $("#main").append(`
      <h4 id="homeMessage">Pas de meuble à afficher pour l'instant !</h4>
    `)
  }
}

function displayStateOfFurniture(state) {
  let furnitureStateString;
  let badgeBgClass = "badge-bg-default"
  switch (state){
    case "PROPO":
      furnitureStateString = "Proposé";
      badgeBgClass = "badge-bg-brownyellow";
      break;
    case "PASCO":
      furnitureStateString = "Ne convient pas";
      badgeBgClass = "badge-bg-red";
      break;
    case "ENRES":
      furnitureStateString = "En restauration";
      badgeBgClass = "badge-bg-lightblue";
      break;
    case "ENMAG":
      furnitureStateString = "En magasin";
      badgeBgClass = "badge-bg-marineblue";
      break;
    case "ENVEN":
      furnitureStateString = "En vente";
      badgeBgClass = "badge-bg-green";
      break;
    case "ENOPT":
      furnitureStateString = "En option";
      badgeBgClass = "badge-bg-darkblue";
      break;
    case "RESER":
      furnitureStateString = "Réservé";
      badgeBgClass = "badge-bg-darkgreen";
      break;
    case "VENDU":
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
  return {state : furnitureStateString, badge: badgeBgClass};
}

function onDisconnection(e) {
    e.preventDefault();
    removeAllDataStorage();
    redirectUrl("/");
}


 

export {displayHome, onDisconnection, onDisplayFurnitures};