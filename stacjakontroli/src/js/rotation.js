import $ from "jquery";

$(document).ready(() => {
    const RC_HTML_ELEMENT = $("#rc");

    RC_HTML_ELEMENT.load('/src/html/rc.html', () => {
        console.log('Formularz rc załadowany');

    });
});
