import {setUserDataStorage} from "../Utils/storage";
import { displayErrorOnThisPage } from "./Error";
import { redirectUrl } from "./Router";
import arrow from "../img/arrow.png";
let registerInformation;

function displayRegister(){
    $("#navbar").empty();
    $("#main").empty();
    $("#main").append(`
    
    <form id="register-form">
        <div class="flexTitle">
        <p class="titleFormRegister"> Compte </p> 
        <img src="${arrow}" alt="name" height="150px" width="200px"/> 
        <p class="other titleFormRegister"> Adresse </p>
        </div>
        pseudo: <input type= "text" class="mt-1 registerInput" id= "usernameRegister" required> </br></br>
        mot de passe: <input type= "password" class="mt-1 registerInput" required id="passwordRegister"> </br></br>
        nom: <input type= "text" class="mt-1 registerInput"  id="nameRegister" required> </br></br>
        prenom: <input type= "text" class="mt-1 registerInput" id="surnameRegister" required> </br></br>
        email:  <input type= "text" class="mt-1 registerInput" id="emailRegister" required> </br></br>
        <div class="col-auto">
            <select class="form-select btn btn-secondary dropdown-toggle" id="roleRegister" >
                <option selected>Rôle souhaité</option>
                <option value="CLI">Client</option>
                <option value="ANT">Antiquaire</option>
                <option value="ADM">Administrateur</option>
            </select>
        </div>
        <button id="submit-registration" class="form-control mt-2">suivant</button>
    </form>`)
    $("#register-form").on("submit", saveNewUserData);
}

function saveNewUserData(e){
    e.preventDefault()

    registerInformation = {
        userName : $("#usernameRegister").val(),
        password : $("#passwordRegister").val(),
        name : $("#nameRegister").val(),
        surname : $("#surnameRegister").val(),
        email : $("#emailRegister").val(),
        role : $("#roleRegister").val(),
        address : null
    }
    displayAdresseForm()
}

function displayAdresseForm(){
    $("#main").empty();
    $("#main").append(`
    <form id="register-form">
        <div class="flexTitle">
        <p class="other titleFormRegister"> Compte </p>
        <img src="${arrow}" alt="name" height="150px" width="200px"/> 
        <p class="titleFormRegister"> Adresse </p>
        </div>
        rue: <input type= "text" class=" mt-1 registerInput" id="streetRegister"  required> </br></br>
        numero: <input type= "text" class=" mt-1 registerInput" id="numberRegister" required> </br></br>
        boite: <input type= "text" class="mt-1 registerInput" id="boxRegister" > </br></br>
        code postal: <input type= "text" class="mt-1 registerInput" id="PostalCodeRegister" required> </br></br>
        commune: <input type= "text" class="mt-1 registerInput" id="communeRegister" required> </br></br>
        pays: <input type= "text" class="mt-1 registerInput" id="countryRegister" required> </br></br>


        <input type="submit" id="submit-registration" class="form-control mt-2" value="s'enregistrer"></button>
    </form>`)
$("#register-form").on("submit", onSubmitRegistration);
}

function onSubmitRegistration(e){
    e.preventDefault();
    registerInformation.address = {
        postalCode : $("#PostalCodeRegister").val(),
        commune : $("#communeRegister").val(),
        street : $("#streetRegister").val(),
        nbr : $("#numberRegister").val(),
        box : $("#boxRegister").val(),
        country : $("#countryRegister").val()
    }
    fetch("/api/authentication/register", {
        method : "POST",
        body : JSON.stringify(registerInformation),
        headers : {"Content-Type" : "application/json"}
    })
    .then((response) => {
        if(!response.ok) {
            let errMsg = "Erreur: "+ response.status + " : " + response.statusText;
            if (response.status == "401") errMsg = "Erreur: Le nom'utilisateur ou le mail est déjà utilisé."
            throw new Error(errMsg);
        }
        
        return response.json();
    })
    .then((data) => onRegister(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function onRegister(data){
    setUserDataStorage(data, false);
    redirectUrl("/");
}

export default displayRegister;