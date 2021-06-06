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
            let self = this;
            makeCall("GET", "GetRecentlyViewedList", null, function (req) {
                if (req.readyState === XMLHttpRequest.DONE) {
                    if (req.status === 200) {
                        let products = JSON.parse(req.responseText);
                        self.update(products);
                    }
                } else {
                    self.alert.textContent = req.responseText;
                }
            })
        }

        this.update = function (products) {
            this.recentlyViewedContainer.innerHTML = "";
            let self = this;
            products.forEach(product => {
                let row = document.createElement("tr");
                let nameTd = document.createElement("td");
                let form = document.createElement("form");
                let button = document.createElement("button");
                form.action = "#";
                button.textContent = product.name;
                button.className = "btn btn-warning";
                button.type = "submit";
                form.appendChild(button);
                nameTd.appendChild(form);
                nameTd.className = "align-content-center";
                row.appendChild(nameTd);
                let categoryTd = document.createElement("td");
                categoryTd.textContent = product.category;
                categoryTd.className = "align-content-center";
                row.appendChild(categoryTd);
                let descriptionTd = document.createElement("td");
                descriptionTd.textContent = product.description;
                descriptionTd.className = "align-content-center";
                row.appendChild(descriptionTd);
                let imageTd = document.createElement("td");
                let image = document.createElement("img");
                image.src = "data:image/png;base64," + product.image;
                image.className = "img-fluid product-thumbnail img-thumbnail";
                imageTd.appendChild(image);
                imageTd.className = "text-center";
                row.appendChild(imageTd);
                self.recentlyViewedContainer.appendChild(row)
            })
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
