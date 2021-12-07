import "./stylesheets/style.css";
import "bootstrap"
import 'bootstrap/dist/css/bootstrap.min.css'
import { getUserStorageData } from "./Utils/storage";
import { me, router } from "./Components/Router";
import "webpack-jquery-ui";

$(window).on("load", () => {
    const dataReturned = getUserStorageData()
    if(!dataReturned) router()
    else me(dataReturned.userCo, dataReturned.isInLocal);
})
