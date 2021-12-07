
function displayErrorOnErrorPage(err) {
    $("#main").empty()
    showError(err)
}

function displayErrorOnThisPage(err) {
    $("#err").remove()
    showError(err)
}

function showError(err) {
    if(err){
        $("#main").append(`<p id="err" class="container alert alert-danger m-5">${err.message}</p>`)
    } 
    else{
        $("#main").append(`<p id="err" class="container alert alert-danger m-5">Erreur: quelque chose s'est mal passé !</p>`)
    } 
}

export {displayErrorOnErrorPage, displayErrorOnThisPage};