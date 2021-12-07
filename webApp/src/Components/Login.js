import { setUserDataStorage } from "../Utils/storage";
import { displayErrorOnThisPage } from "./Error";
import { redirectUrl } from "./Router";
import LiViSatcho from "../img/LiViSatcho.png";

function displayLogin() {
    $("#navbar").empty();
    $("#main").empty();
    $("#main").append(`
    <form id="login-form">
        <img id="nomLogin" src="${LiViSatcho}" alt="name" height="150px" width="550px"/> 
        <div class="form-group container mt-5">
            Pseudo : <input type="text" class="registerInput mt-1" id="login-pseudo" required></br></br>
            Mot de passe : <input type="password" class="registerInput mt-1" id="login-password" required></br></br>
            Se souvenir de moi : <input type="checkbox" id="login-rememberMe">
            <input type="submit" id= "submitLogin" class="form-control mt-2" value="Se connecter">
        </div>
    </form>
    `);
    $("#login-form").on("submit", onSubmitLogin);
}

function onSubmitLogin(e) {
    e.preventDefault();
    let user = {
        userName : $("#login-pseudo").val(),
        password : $("#login-password").val()
    }
    fetch("/api/authentication/login", {
        method : "POST",
        body : JSON.stringify(user),
        headers : {"Content-Type" : "application/json"}
    })
    .then((response) => {
        if(!response.ok){
            let txtErr = "Erreur: " + response.status + " : " + response.statusText;
            if(response.status == "401") txtErr = "Erreur: Mauvais mot de passe ou nom d'utilisateur."
            throw new Error(txtErr);
        } 
        return response.json();
    })
    .then((data) => onLogin(data))
    .catch((err) => displayErrorOnThisPage(err))
}

function onLogin(data) {
    setUserDataStorage(data, $("#login-rememberMe")[0].checked)
    redirectUrl("/")
}

export {displayLogin};