/**
 * basic checking file type 
 * source : https://stackoverflow.com/questions/18299806/how-to-check-file-mime-type-with-javascript-before-upload
 * @param {*} file file to check
 */
 function verifyType(file, callback) {
    let typeRegex = RegExp("image/*")
    if (!typeRegex.test(file.type)) return callback(false);
    verifyMIME(file, (mimeTypeOk) => {
        callback(mimeTypeOk)
    })
}


/**
 * MIME type checking
 * source : https://stackoverflow.com/questions/18299806/how-to-check-file-mime-type-with-javascript-before-upload
 * @param {*} file file to check
 */
function verifyMIME(file, callback) {
    let blob = file;
    let type;
    let fileReader = new FileReader();
    fileReader.onloadend = function (e) {
        let arr = (new Uint8Array(e.target.result)).subarray(0, 4);
        let header = "";
        for (let i = 0; i < arr.length; i++) {
            header += arr[i].toString(16);
        }
        type = imageMIMETypes(header)
        callback(type !== "unknown")
    };
    fileReader.readAsArrayBuffer(blob);
}

/**
 * Check the 4 first bytes of an image in order to determine which type is the file thanks to his header
 * source : https://stackoverflow.com/questions/18299806/how-to-check-file-mime-type-with-javascript-before-upload
 * @param {*} header header of the file
 */
function imageMIMETypes(header) {
    let type = "unknown";
    switch (header) {
        case "89504e47":
            type = "image/png";
            break;
        case "ffd8ffe0":
        case "ffd8ffe1":
        case "ffd8ffe2":
        case "ffd8ffe3":
        case "ffd8ffe8":
            type = "image/jpeg";
            break;
        case "00000100":
        default:
            type = "unknown";
            break;
    }
    return type;
}

export default verifyType;