(function() { // avoid variables ending up in the global scope

    // page components
    let navBar, home,
        pageOrchestrator = new PageOrchestrator(); // main controller

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
            window.location.href = "index.html";
        } else {
            pageOrchestrator.start(); // initialize the components
            pageOrchestrator.refresh();
        } // display initial content
    }, false);


    // Constructors of view components

    function NavBar() {
        this.registerEvents = function () {
            document.getElementById("homeLink").addEventListener('click', ev => {
                pageOrchestrator.navigateTo(home)
            })
            document.getElementById("cartLink").addEventListener('click', ev => {
                // pageOrchestrator.navigateTo(cart)
            })
            document.getElementById("ordersLink").addEventListener('click', ev => {
                // pageOrchestrator.navigateTo(orders)
            })
            document.getElementById("logoutLink").addEventListener('click', ev => {
                window.sessionStorage.removeItem('username');
            })
        }
    }

    function Home(_username, _usernameContainer, _recentlyViewedContainer) {
        this.username = _username;
        this.usernameContainer = _usernameContainer;
        this.recentlyViewedContainer = _recentlyViewedContainer;

        this.show = function() {
            this.usernameContainer.textContent = this.username;
        }
    }

    function PageOrchestrator() {
        let alertContainer = document.getElementById("id_alert");
        this.start = function() {
            navBar = new NavBar()
            navBar.registerEvents()

            home = new Home(sessionStorage.getItem('username'),
                document.getElementById("username"), document.getElementById("recentlyViewed"));
            home.show();
        };

        this.refresh = function() {
            //alertContainer.textContent = "";
        };

        this.navigateTo = function (page) {
            // TODO reset all other pages
            page.show()
        }
    }
})();
