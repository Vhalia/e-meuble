/**
 * convert a date to switch year and date position
 */
 function convert(date){
    var datearray = date.split("-");
    var newdate = datearray[2] + '-' + datearray[1] + '-' + datearray[0];
    return newdate;
    
}

/**
 * convert a date from the back to have the correct showed format
 */
function convertToLocalDate(date){
    let d = new Date (convert(date));
    d.setDate(d.getDate()+1);
    return d.toLocaleDateString();
}

export {convert, convertToLocalDate}