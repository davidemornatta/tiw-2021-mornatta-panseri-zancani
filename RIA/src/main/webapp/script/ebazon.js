(function() { // avoid variables ending up in the global scope

    // Cart
    let cart;
    // page components
    let navBar, alertContainer, alertText, home, searchResults, supplierPopUp, supplierPopUpText, cartPage,
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
            document.getElementById("homeLink").addEventListener('click', () => {
                pageOrchestrator.navigateTo(home)
            })
            document.getElementById("cartLink").addEventListener('click', () => {
                // pageOrchestrator.navigateTo(cart)
            })
            document.getElementById("ordersLink").addEventListener('click', () => {
                // pageOrchestrator.navigateTo(orders)
            })
            document.getElementById("logoutLink").addEventListener('click', () => {
                window.sessionStorage.removeItem('username');
            })
            let searchButton = document.getElementById("searchButton");
            searchButton.addEventListener('click', () => {
                let searchQuery = document.getElementById("searchInput").value;
                if(searchQuery.length !== 0)
                    pageOrchestrator.navigateTo(searchResults, searchQuery, null);
            })
        }
    }

    function Home(_pageContainer, _username, _usernameContainer, _recentlyViewedContainer) {
        this.pageContainer = _pageContainer;
        this.username = _username;
        this.usernameContainer = _usernameContainer;
        this.recentlyViewedContainer = _recentlyViewedContainer;

        this.show = function() {
            this.usernameContainer.textContent = this.username;
            let self = this;
            this.pageContainer.className = "";
            makeCall("GET", "GetRecentlyViewedList", null, function (req) {
                if (req.readyState === XMLHttpRequest.DONE) {
                    if (req.status === 200) {
                        let products = JSON.parse(req.responseText);
                        self.update(products);
                    } else if (req.status === 401) {
                        sessionStorage.removeItem("username");
                        window.location.href = "index.html";
                    } else {
                        alertText.textContent = req.responseText;
                        alertContainer.className = "";
                    }
                }
            })
        }

        this.update = function (products) {
            this.recentlyViewedContainer.innerHTML = "";
            let self = this;
            products.forEach(product => {
                let row = document.createElement("tr");
                let nameTd = document.createElement("td");
                let button = document.createElement("button");
                button.textContent = product.name;
                button.className = "btn btn-warning";
                button.addEventListener('click', () => {
                    pageOrchestrator.navigateTo(searchResults, null, product.code);
                });
                nameTd.appendChild(button);
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

        this.reset = function () {
            this.recentlyViewedContainer.innerHTML = "";
            this.pageContainer.className = "hidden";
        }
    }

    function SearchResults(_pageContainer, _searchResultsContainer, _productDetailsContainer, _productDetailsTable,
                           _suppliersTable) {
        this.pageContainer = _pageContainer;
        this.searchResultsContainer = _searchResultsContainer;
        this.productDetailsContainer = _productDetailsContainer;
        this.productDetailsTable = _productDetailsTable;
        this.suppliersTable = _suppliersTable;

        this.lastSearchQuery = "";

        this.show = function (searchQuery, selectedProduct) {
            let self = this;
            this.pageContainer.className = "";
            this.searchResultsContainer.style.visibility = "hidden";
            this.productDetailsContainer.style.visibility = "hidden";
            if(searchQuery != null) {
                this.searchResultsContainer.style.visibility = "visible";
                this.lastSearchQuery = searchQuery;
                makeCall("GET", "GetSearchResultsData?searchQuery=" + searchQuery, null,
                    function (req) {
                        if (req.readyState === XMLHttpRequest.DONE) {
                            if (req.status === 200) {
                                let products = JSON.parse(req.responseText);
                                self.update(products, null);
                            } else if (req.status === 401) {
                                sessionStorage.removeItem("username");
                                window.location.href = "index.html";
                            } else {
                                alertText.textContent = req.responseText;
                                alertContainer.className = "";
                            }
                        }
                    })
            }
            if(selectedProduct != null) {
                this.productDetailsContainer.style.visibility = "visible";
                makeCall("GET", "GetProductDetailsData?productCode=" + selectedProduct, null,
                    function (req) {
                        if (req.readyState === XMLHttpRequest.DONE) {
                            if (req.status === 200) {
                                let details = JSON.parse(req.responseText);
                                self.update(null, details);
                            } else if (req.status === 401) {
                                sessionStorage.removeItem("username");
                                window.location.href = "index.html";
                            } else {
                                alertText.textContent = req.responseText;
                                alertContainer.className = "";
                            }
                        }
                    })
            }
        }

        this.update = function (results, productDetails) {
            let self = this;
            if(results != null) {
                let list = this.searchResultsContainer.querySelector("ul");
                list.innerHTML = "";
                results.forEach(object => {
                    let product = object.product;
                    let price = object.price;

                    let item = document.createElement("li");
                    item.className = "list-group-item";
                    let text = document.createElement("span");
                    text.innerHTML = product.name + " - " + product.code + "<br>" + "Min price: &#36;" + price + "<br>";
                    item.appendChild(text);
                    let button = document.createElement("button");
                    button.className = "btn btn-primary";
                    button.innerText = "View Details";
                    button.addEventListener('click', () => {
                        pageOrchestrator.navigateTo(searchResults, self.lastSearchQuery, product.code);
                    })
                    item.appendChild(button);

                    list.appendChild(item);
                })
            }
            if(productDetails != null) {
                let product = productDetails.product;
                let row = this.productDetailsTable.querySelector("tbody tr");
                row.innerHTML = "";

                let name = document.createElement("td");
                name.className = "align-content-center";
                name.innerText = product.name;
                row.appendChild(name);

                let category = document.createElement("td");
                category.className = "align-content-center";
                category.innerText = product.category;
                row.appendChild(category);

                let description = document.createElement("td");
                description.className = "align-content-center";
                description.innerText = product.description;
                row.appendChild(description);

                let imageTd = document.createElement("td");
                let image = document.createElement("img");
                image.src = "data:image/png;base64," + product.image;
                image.className = "img-fluid product-thumbnail img-thumbnail";
                imageTd.appendChild(image);
                imageTd.className = "text-center";
                row.appendChild(imageTd);

                let suppliers = productDetails.suppliers;
                let tbody = this.suppliersTable.querySelector("tbody");
                tbody.innerHTML = "";

                suppliers.forEach(supplier => {
                    let row = document.createElement("tr");

                    let name = document.createElement("td");
                    name.className = "align-content-center";
                    name.innerText = supplier.name;
                    row.appendChild(name);

                    let rating = document.createElement("td");
                    rating.className = "align-content-center";
                    rating.innerText = supplier.rating;
                    row.appendChild(rating);

                    let price = document.createElement("td");
                    price.className = "align-content-center";
                    price.innerHTML = "&#36;" + supplier.price;
                    row.appendChild(price);

                    let rangesTd = document.createElement("td");
                    rangesTd.className = "align-content-center";
                    let rangesUl = document.createElement("ul");
                    rangesUl.className = "list-group list-group-flush";
                    supplier.priceRanges.forEach(r => {
                        let range = document.createElement("li");
                        range.innerText = r;
                        rangesUl.appendChild(range);
                    })
                    rangesTd.appendChild(rangesUl);
                    row.appendChild(rangesTd);

                    let freeShipping = document.createElement("td");
                    freeShipping.className = "align-content-center";
                    freeShipping.innerHTML = "&#36;" + supplier.freeShippingCost;
                    row.appendChild(freeShipping);

                    let productInCart = document.createElement("td");
                    productInCart.className = "align-content-center";
                    let supplierMap = cart[supplier.code];
                    let productSum = 0;
                    if(supplierMap !== undefined) {
                        for (const [, value] of Object.entries(supplierMap)) {
                            productSum += value;
                        }
                    }
                    productInCart.innerText = "" + productSum;
                    productInCart.onmouseover = function (mouseEvent) {
                        supplierPopUpText.textContent = "CIAO";
                        supplierPopUp.style.left = mouseEvent.x + 'px';
                        supplierPopUp.style.top = mouseEvent.y + 'px';
                        supplierPopUp.className = "";
                    }
                    productInCart.onmouseout = function () {
                        supplierPopUpText.textContent = "";
                        supplierPopUp.className = "hidden";
                    }
                    row.appendChild(productInCart);

                    let totInCart = document.createElement("td");
                    totInCart.className = "align-content-center";
                    makeCall("GET", "GetCartPrices?cart=" + btoa(JSON.stringify(cart)), null, function (req) {
                        if (req.readyState === XMLHttpRequest.DONE) {
                            if (req.status === 200) {
                                let prices = JSON.parse(req.responseText);
                                totInCart.innerHTML = "&#36;" + prices[supplier.name].productsTotal;
                            } else if (req.status === 401) {
                                sessionStorage.removeItem("username");
                                window.location.href = "index.html";
                            } else {
                                alertText.textContent = req.responseText;
                                alertContainer.className = "";
                            }
                        }
                    })
                    row.appendChild(totInCart);

                    let formTd = document.createElement("td");
                    formTd.className = "align-content-center";
                    let addToCartForm = document.createElement("form");
                    addToCartForm.action = "#";
                    let label = document.createElement("label");
                    label.innerText = "Quantity: ";
                    let input = document.createElement("input");
                    input.type = "number";
                    input.name = "quantity";
                    input.min = "1";
                    input.value = "1";
                    label.appendChild(input);
                    addToCartForm.appendChild(label);
                    let button = document.createElement("button");
                    button.type = "button";
                    button.className = "btn btn-primary";
                    button.innerText = "Add to Cart";
                    button.addEventListener('click', () => {
                        let supplierMap = cart[supplier.code];
                        if(supplierMap === undefined) {
                            supplierMap = {};
                            cart[supplier.code] = supplierMap;
                        }

                        if(supplierMap[product.code] === undefined) {
                            supplierMap[product.code] = parseInt(input.value);
                        } else {
                            supplierMap[product.code] = supplierMap[product.code] + parseInt(input.value);
                        }

                        localStorage.setItem("cart", JSON.stringify(cart));

                        pageOrchestrator.navigateTo(cartPage);
                    })
                    addToCartForm.appendChild(button);
                    formTd.appendChild(addToCartForm);
                    row.appendChild(formTd);

                    tbody.appendChild(row);
                })
            }
        }

        this.reset = function () {
            this.pageContainer.className = "hidden";
            this.searchResultsContainer.style.visibility = "hidden";
            this.productDetailsContainer.style.visibility = "hidden";
        }
    }

    function CartPage(_pageContainer) {
        this.pageContainer = _pageContainer;

        this.show = function () {
            this.pageContainer.className = "";
            let self = this;
            makeCall("GET", "GetCartPrices?cart=" + btoa(JSON.stringify(cart)), null, function (req) {
                if (req.readyState === XMLHttpRequest.DONE) {
                    if (req.status === 200) {
                        let prices = JSON.parse(req.responseText);
                        self.update(prices);
                    } else if (req.status === 401) {
                        sessionStorage.removeItem("username");
                        window.location.href = "index.html";
                    } else {
                        alertText.textContent = req.responseText;
                        alertContainer.className = "";
                    }
                }
            })
        }

        this.update = function (prices) {
            console.log(prices)
        }

        this.reset = function () {
            this.pageContainer.className = "hidden";
        }
    }

    function PageOrchestrator() {
        alertContainer = document.getElementById("alertBox");
        alertText = document.getElementById("alertText");
        supplierPopUp = document.getElementById("supplierPopUp");
        supplierPopUpText = document.getElementById("supplierPopUpText");
        let alertCloseButton = document.getElementById("closeButton");
        alertCloseButton.addEventListener('click', () => alertContainer.className = "hidden");
        this.start = function() {
            navBar = new NavBar()
            navBar.registerEvents()

            home = new Home(document.getElementById("homePage"), sessionStorage.getItem('username'),
                document.getElementById("username"), document.getElementById("recentlyViewed"));
            home.show();

            searchResults = new SearchResults(document.getElementById("searchPage"),
                document.getElementById("searchResults"), document.getElementById("productDetails"),
                document.getElementById("productDetailsTable"), document.getElementById("suppliersTable"));

            cartPage = new CartPage(document.getElementById("cartPage"));

            if(localStorage.getItem("cart") !== null) {
                cart = JSON.parse(localStorage.getItem("cart"));
            } else {
                cart = {}
            }
        };

        this.refresh = function() {
            alertText.textContent = "";
            alertContainer.className = "hidden";
            supplierPopUpText.textContent = "";
            supplierPopUp.className = "hidden";
        };

        this.navigateTo = function (page, ...args) {
            home.reset();
            searchResults.reset();
            cartPage.reset();
            page.show(...args);
        }
    }
})();
