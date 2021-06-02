/**
 * Login management
 */
(function() { // avoid variables ending up in the global scope
    document.getElementById("loginButton").addEventListener('click', (e) => {
        let form = e.target.closest("form");
        if (form.checkValidity()) {
            makeCall("POST", 'CheckLogin', e.target.closest("form"),
                function(req) {
                    if (req.readyState === XMLHttpRequest.DONE) {
                        let message = req.responseText;
                        switch (req.status) {
                            case 200:
                                sessionStorage.setItem('username', message);
                                window.location.href = "home.html";
                                break;
                            case 400: // bad request
                                document.getElementById("errorMessage").textContent = message;
                                break;
                            case 401: // unauthorized
                                document.getElementById("errorMessage").textContent = message;
                                break;
                            case 500: // server error
                                document.getElementById("errorMessage").textContent = message;
                                break;
                        }
                    }
                }
            );
        } else {
            form.reportValidity();
        }
    });
})();