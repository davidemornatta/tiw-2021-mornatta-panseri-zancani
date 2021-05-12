#Components
- Model objects (Beans)
  * User
  * Product
  * Supplier
  * PriceRange
  * Order
  * Cart
    * findProductQuantityFor(supplierCode)
    * findProductTotalFor(supplierCode)
- Data Access objects (Classes)
  * UserDAO
    * checkCredentials(username, password)
    * findLastFiveViewedBy(userId)
    * addViewToProductFrom(userId, productCode)
  * ProductDAO
    * findProductByCode(code)
    * searchForProduct(searchQuery)
  * SupplierDAO
    * findSupplierByCode(code)
    * findMinPrice(productCode)
    * findAllSuppliers(productCode)
  * PriceRangeDAO
    * findPriceRangesForSupplier(supplierCode)
  * OrderDAO
    * createOrder(order, userId)
    * findUserOrders(userId)
    * findAllProductsInOrder(orderCode)
- Controllers (Servlets)
  * CheckLogin
  * GoToHome
  * GoToOrders
  * GoToShoppingCart
  * GoToSearchResults
  * UpdateCart
  * ProcessOrder
- Views (Templates)
  * LoginPage
  * Home
  * Navbar
  * Orders
  * SearchResults
  * ShoppingCart

