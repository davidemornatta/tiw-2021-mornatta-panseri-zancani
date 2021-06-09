(function() { // avoid variables ending up in the global scope

    // Cart
    let cart;
    // page components
    let navBar, alertContainer, alertText, home, searchResults, supplierPopUp, supplierPopUpText, cartPage, orderPage,
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
            document.getElementById("brand").addEventListener('click', () => {
                pageOrchestrator.navigateTo(home)
            })
            document.getElementById("homeLink").addEventListener('click', () => {
                pageOrchestrator.navigateTo(home)
            })
            document.getElementById("cartLink").addEventListener('click', () => {
                pageOrchestrator.navigateTo(cartPage)
            })
            document.getElementById("ordersLink").addEventListener('click', () => {
                 pageOrchestrator.navigateTo(orderPage)
            })
            document.getElementById("logoutLink").addEventListener('click', () => {
                makeCall("POST", "Logout", null, function () {
                    window.sessionStorage.removeItem('username');
                    window.location.href = "index.html";
                })

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

                let self = this;
                makeCall("GET", "GetCartPrices?cart=" + btoa(JSON.stringify(cart)), null, function (req) {
                    if (req.readyState === XMLHttpRequest.DONE) {
                        if (req.status === 200) {
                            self.prices = JSON.parse(req.responseText);
                            self.createDetails(product, suppliers, self.prices, tbody);
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

        this.createDetails = function (product, suppliers, prices, tbody) {
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
                    if(prices[supplier.name] !== undefined) {
                        supplierPopUp.appendChild(cartPage.buildSupplierCart(supplier.name, prices))
                        supplierPopUp.style.left = mouseEvent.x + 'px';
                        supplierPopUp.style.top = (mouseEvent.y-300) + 'px';
                        supplierPopUp.className = "";
                    }
                }
                productInCart.onmouseout = function () {
                    supplierPopUp.innerHTML = "";
                    supplierPopUp.className = "hidden";
                }
                row.appendChild(productInCart);

                let totInCart = document.createElement("td");
                totInCart.className = "align-content-center";
                makeCall("GET", "GetCartPrices?cart=" + btoa(JSON.stringify(cart)), null, function (req) {
                    if (req.readyState === XMLHttpRequest.DONE) {
                        if (req.status === 200) {
                            let prices = JSON.parse(req.responseText);
                            if(prices[supplier.name] !== undefined)
                                totInCart.innerHTML = "&#36;" + prices[supplier.name].productsTotal;
                            else
                                totInCart.innerHTML = "&#36;0";
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

        this.reset = function () {
            this.pageContainer.className = "hidden";
            this.searchResultsContainer.style.visibility = "hidden";
            this.productDetailsContainer.style.visibility = "hidden";
        }
    }

    function CartPage(_pageContainer, _listContainer) {
        this.pageContainer = _pageContainer;
        this.listContainer = _listContainer;

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
            this.listContainer.innerHTML = "";
            for (const supplier in prices) {
                if (!prices.hasOwnProperty(supplier))
                    continue;
                this.listContainer.appendChild(this.buildSupplierCart(supplier, prices, true));
            }

        }

        this.buildSupplierCart = function (supplier, prices, orderButton = false) {
            let div = document.createElement("div");
            div.className = "card navbar-blue";

            let title = document.createElement("h3");
            title.className = "orange-text p-2";
            title.innerText = supplier + " - " + prices[supplier].products.length + " products";
            div.appendChild(title);

            let innerDiv = document.createElement("div");
            innerDiv.className = "lightGrey p-3";

            let priceTable = document.createElement("table");
            priceTable.className = "table align-middle text-center";

            let priceTHead = document.createElement("thead");
            let priceTHeadTr = document.createElement("tr");
            priceTHead.appendChild(priceTHeadTr);
            let priceTHeadTotal = document.createElement("th");
            priceTHeadTotal.innerText = "Total";
            priceTHeadTr.appendChild(priceTHeadTotal);
            let priceTHeadShipping = document.createElement("th");
            priceTHeadShipping.innerText = "Shipping";
            priceTHeadTr.appendChild(priceTHeadShipping);
            priceTHead.appendChild(priceTHeadTr);
            priceTable.appendChild(priceTHead);

            let priceTBody = document.createElement("tbody");
            let priceTBodyTr = document.createElement("tr");
            let priceTBodyTotal = document.createElement("td");
            priceTBodyTotal.innerHTML = "&#36;" + prices[supplier].productsTotal;
            priceTBodyTr.appendChild(priceTBodyTotal);
            let priceTBodyShipping = document.createElement("td");
            priceTBodyShipping.innerHTML = "&#36;" + prices[supplier].shippingTotal;
            priceTBodyTr.appendChild(priceTBodyShipping);
            priceTBody.appendChild(priceTBodyTr);
            priceTable.appendChild(priceTBody);

            innerDiv.appendChild(priceTable);

            let productsTable = document.createElement("table");
            productsTable.className = "table align-middle text-center";

            let productsTHead = document.createElement("thead");
            let productsTHeadTr = document.createElement("tr");
            productsTHead.appendChild(productsTHeadTr);
            let productsTHeadName = document.createElement("th");
            productsTHeadName.innerText = "Product";
            productsTHeadTr.appendChild(productsTHeadName);
            let productsTHeadQuantity = document.createElement("th");
            productsTHeadQuantity.innerText = "Quantity";
            productsTHeadTr.appendChild(productsTHeadQuantity);
            productsTHead.appendChild(productsTHeadTr);
            productsTable.appendChild(productsTHead);

            let productsTBody = document.createElement("tbody");
            prices[supplier].products.forEach(product => {
                let productsTBodyTr = document.createElement("tr");
                let productsTBodyName = document.createElement("td");
                productsTBodyName.innerText = product.name;
                productsTBodyTr.appendChild(productsTBodyName);
                let priceTBodyQuantity = document.createElement("td");
                priceTBodyQuantity.innerText = product.quantity;
                productsTBodyTr.appendChild(priceTBodyQuantity);
                productsTBody.appendChild(productsTBodyTr);
            })
            productsTable.appendChild(productsTBody);

            innerDiv.appendChild(productsTable);

            if(orderButton) {
                let orderForm = document.createElement("form");
                orderForm.action = "#";
                let supplierInput = document.createElement("input");
                supplierInput.type = "hidden";
                supplierInput.name = "supplier";
                supplierInput.value = prices[supplier].code;
                orderForm.appendChild(supplierInput);
                let cartInput = document.createElement("input");
                cartInput.type = "hidden";
                cartInput.name = "cart";
                cartInput.value = btoa(JSON.stringify(cart));
                orderForm.appendChild(cartInput);
                let orderBtn = document.createElement("button");
                orderBtn.className = "btn btn-primary";
                orderBtn.type = "button";
                orderBtn.innerText = "Order";

                orderBtn.addEventListener('click', () => {
                    makeCall("POST", "CreateOrder", orderForm, function (req) {
                        if (req.readyState === XMLHttpRequest.DONE) {
                            if (req.status === 200) {
                                delete cart[prices[supplier].code];
                                localStorage.setItem("cart", JSON.stringify(cart));
                                // pageOrchestrator.navigateTo(orders);
                            } else if (req.status === 401) {
                                sessionStorage.removeItem("username");
                                window.location.href = "index.html";
                            } else {
                                alertText.textContent = req.responseText;
                                alertContainer.className = "";
                            }
                        }
                    });
                });
                orderForm.appendChild(orderBtn);
                innerDiv.appendChild(orderForm);
            }

            div.appendChild(innerDiv);
            return div;
        }

        this.reset = function () {
            this.pageContainer.className = "hidden";
        }
    }


    function OrderPage(_pageContainer, _listContainer) {
        this.pageContainer = _pageContainer;
        this.listContainer = _listContainer;



        this.show = function () {
            this.pageContainer.className = "";
            let self = this;
            makeCall("GET", "GetOrderList", null, function (req) {
                if (req.readyState === XMLHttpRequest.DONE) {
                    if (req.status === 200) {
                        let orderProducts = JSON.parse(req.responseText);
                        self.update(orderProducts);
                    } else {
                        alertText.textContent = req.responseText;
                        alertContainer.className = "";
                    }
                }
            })
        }

        this.update = function (orderProducts) {
            console.log(orderProducts);
            this.listContainer.innerHTML = "";
            let self = this;
            let orderList = document.createElement("ul");
            orderList.className+="list-group list-group-flush lightGrey-text ";
            let orders = orderProducts;
            let liList = document.createElement("li");
            for (const order in orders) {
                let orderTable = document.createElement("table");
                let orderDetails = document.createElement("tr");
                let space = document.createElement("td");
                space.style.width="10%";
                let orderText = document.createElement("td");
                orderText.style.width="25%";
                let orderCode = document.createElement("ul");
                orderCode.innerText = "Order Code: "+ orders[order].orderDetails.orderCode;
                let suppCode = document.createElement("ul");
                suppCode.innerText = "Supplier Code: " + orders[order].orderDetails.suppCode ;
                let tot = document.createElement("ul");
                tot.innerText = "Total: "+  orders[order].orderDetails.totalAmount ;
                let shippingDate = document.createElement("ul");
                shippingDate.innerText = "Shipping Date: " + orders[order].orderDetails.shippingDate;
                let shippingAddress = document.createElement("ul");
                shippingAddress.innerText = "Shipping Address: "+  orders[order].orderDetails.shippingAddress ;
                orderText.appendChild(orderCode);
                orderText.appendChild(suppCode);
                orderText.appendChild(tot);
                orderText.appendChild(shippingDate);
                orderText.appendChild(shippingAddress);
                orderDetails.appendChild(space);
                orderDetails.appendChild(orderText);
                orderDetails.appendChild(space);
                orderTable.appendChild(orderDetails);

                let ProdQCol= document.createElement("td");
                let prodTable = document.createElement("table");
                 prodTable.className+="table table-bordered lightGrey-text";
                let tableHead = document.createElement("thead");
                 tableHead.className+="lightBlue";
                let columnNames = document.createElement("tr");
                let c1 = document.createElement("th");
                c1.innerText="Product";
                let c2 = document.createElement("th");
                c2.innerText="Quantity";
                columnNames.appendChild(c1);
                columnNames.appendChild(c2);
                tableHead.appendChild(columnNames);
                prodTable.appendChild(tableHead);

                  let body = document.createElement("tbody");
                  body.className+="table-light";

                orders[order].prodQuantity.forEach(product => {
                    let productsQTr = document.createElement("tr");
                    let productName = document.createElement("td");
                    productName.style.width="65%";
                    productName.innerText = product.name;
                    productsQTr.appendChild(productName);
                    let productQuantity = document.createElement("td");
                    productQuantity.innerText = product.quantity;
                    productsQTr.appendChild(productQuantity);
                    body.appendChild(productsQTr);
                })

                prodTable.appendChild(body);


                ProdQCol.appendChild(prodTable);
                orderDetails.appendChild(ProdQCol);
                orderTable.appendChild(orderDetails);
                liList.appendChild(orderTable);



            }
                orderList.appendChild(liList);
                this.listContainer.appendChild(orderList);
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

            cartPage = new CartPage(document.getElementById("cartPage"), document.getElementById("cartContainer"));

            orderPage = new OrderPage(document.getElementById("orderPage"),document.getElementById("ordersContainer"));

            if(localStorage.getItem("cart") !== null) {
                cart = JSON.parse(localStorage.getItem("cart"));
            } else {
                cart = {}
            }
        };

        this.refresh = function() {
            alertText.textContent = "";
            alertContainer.className = "hidden";
            supplierPopUp.className = "hidden";
        };

        this.navigateTo = function (page, ...args) {
            home.reset();
            searchResults.reset();
            cartPage.reset();
            orderPage.reset();
            page.show(...args);
        }
    }
})();
