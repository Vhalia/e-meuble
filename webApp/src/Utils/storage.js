const STORAGE_USER_KEY = "user_co";

/**
 * Set the user's data in the local/session storage when he's logged on
 * @param {*} userData data of the user
 * @param {*} inLocal boolean which determines if the "remember me" is checked
 */
function setUserDataStorage(userData, inLocal){
    if(inLocal) localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(userData));
    else sessionStorage.setItem(STORAGE_USER_KEY, JSON.stringify(userData));
}

/**
 * Retrieve user's data from the storage
 */
function getUserStorageData ()  {
    let inLocal = true;
    let user = localStorage.getItem(STORAGE_USER_KEY);
    if(!user){
        user = sessionStorage.getItem(STORAGE_USER_KEY);
        inLocal = false;
    } 
    if (!user) return;
    return {userCo : JSON.parse(user), isInLocal : inLocal};
};

/**
 * Remove all the content in the local storage
 */
function removeAllDataStorage() {
    localStorage.removeItem(STORAGE_USER_KEY);
    sessionStorage.removeItem(STORAGE_USER_KEY);
    
}
export {getUserStorageData, removeAllDataStorage, setUserDataStorage}