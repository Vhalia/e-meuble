import {
    getUserStorageData,
    removeAllDataStorage,
    setUserDataStorage
} from "../Utils/storage";
import { displayErrorOnErrorPage, displayErrorOnThisPage } from "./Error";
import {displayHome} from "./Home";
import displayFurniture from "./Furniture"
import {displayLogin} from "./Login";
import displayRegister from "./Register";
import displayAdmin from "./Admin";
import {displayProfil} from "./Profil";
import displayViewProfil from "./ViewProfil";
import displayVisitRequest from "./VisitRequest";
import displayResultResearch from "./Research.js";
import displayVisit from "./ViewVisit";


const routes = {
    "/" : displayHome,
    "/login" : displayLogin,   
    "/furnitures" : displayFurniture,
    "/register": displayRegister,
    "/admin" : displayAdmin,
    "/profil" : displayProfil,
    "/viewProfil" : displayViewProfil,
    "/visitRequest": displayVisitRequest,
    "/error": displayErrorOnErrorPage,
    "/research": displayResultResearch,
    "/visit": displayVisit
}

let pageToRender;

/**
 * allow to render the right page in relation with the current url
 */

function router() {
    pageToRender = routes[window.location.pathname];
    if (!pageToRender) {
        displayErrorOnErrorPage(new Error(`Erreur: l'url ${window.location.pathname} n'existe pas`));
        return;
    }
    pageToRender();

    $(window).on("popstate", () => {
        pageToRender = routes[window.location.pathname]
        pageToRender();
    })
}

/**
 * Change the url when interacting with DOM elements which have data-url and render the right page
 * @param {*} e event 
 */
function onNavigate(e) {
    let url;
    let value;
    let searchQueryKey;
    if (e.target.parentElement.dataset.aim == "viewFurniturePage") {
        url = e.target.parentElement.dataset.url
        searchQueryKey = e.target.parentElement.dataset.querykey;
        value = e.target.parentElement.dataset.id;
    }else if (e.target.tagName === "A") {
        e.preventDefault();
        url = e.target.dataset.url;
    } else if (e.target.tagName === "BUTTON") {
        url = e.target.dataset.url
    }else if (e.target.tagName === "IMG" || e.target.tagName === "P"){
        searchQueryKey = "idMeuble";
        url = e.target.parentElement.dataset.url
        if(!url) url = e.target.dataset.url
        value = e.target.parentElement.dataset.id;
    }else if (e.target.tagName === "TD") {
        searchQueryKey = e.target.parentElement.dataset.querykey;
        if(!searchQueryKey) searchQueryKey = e.target.dataset.querykey;
        url = e.target.parentElement.dataset.url
        if(!url) url = e.target.dataset.url
        value = e.target.parentElement.dataset.id;
    }else if (e.target.tagName === "FORM") {
        e.preventDefault();
        searchQueryKey = "keyword";
        url = e.target.dataset.url
        value = e.target.dataset.keyword;
    }
    if (url) {
        if(value) {
            window.history.pushState({}, url, window.location.origin + url + '?'+searchQueryKey+'=' + value) 
         }
          else window.history.pushState({}, url, window.location.origin + url)
        pageToRender = routes[url];

        if (routes[url]) {
            pageToRender();
        } else {
            displayErrorOnErrorPage(new Error(`Erreur: L'url ${url} n'existe pas`))
        }
    }
}

/**
 * Redirect to the url url
 * @param {*} url url to redirect
 */
function redirectUrl(url) {
    window.history.pushState({}, url, window.location.origin + url)

    pageToRender = routes[url]
    if (routes[url]) {
        pageToRender();
    } else {
        displayErrorOnErrorPage(new Error(`Erreur: L'url ${url} n'existe pas`))
    }
}

function me(user, isInLocal) {
    if (!user) return;
    fetch("/api/users/me", {
            method: "GET",
            headers: {
                Authorization: user.token
            }
        })
        .then((response) => {
            if (!response.ok) throw new Error("Erreur: " + response.status + " : " + response.statusText);
            return response.json();
        })
        .then((data) => {
            setUserDataStorage(data, isInLocal)
            router()
        })
        .catch((err) => {
            removeAllDataStorage()
            redirectUrl("/login")
            displayErrorOnThisPage(err)
        });
}

export {
    me,
    router,
    redirectUrl,
    onNavigate
};
