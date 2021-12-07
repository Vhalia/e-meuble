
/**
 * Return the value associated with the key parameterName in the url
 * @param {*} parameterName the key
 * //source : https://stackoverflow.com/questions/5448545/how-to-retrieve-get-parameters-from-javascript
 */
 function findGetParameter(parameterName) {
    var result = null,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
          tmp = item.split("=");
          if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}

export default findGetParameter;