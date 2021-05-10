#Components
- Model objects (Beans)
  * User
  * Product
  * Supplier
  * PriceRange
  * Order
- Data Access objects (Classes)
  * UserDAO
    * checkCredentials(username, password)
  * ProductDAO
    * findProductByCode(code)
    * searchForProduct(searchQuery)
  * SupplierDAO
    * findSupplierByCode(code)
  * PriceRangeDAO
    * findPriceRangesForSupplier(supplierCode)
  * OrderDAO
    * createOrder(order, userId)
    * findUserOrders(userId)
  * RecentlyViewedDAO
    * findLastFiveViewedBy(userId)
    * addViewToProductFrom(userId, productCode)
  * OrderContainsDAO
    * findAllProductsInOrder(orderCode)
  * SellsDAO
    * findMinPrice(productCode)
    * findAllSuppliers(productCode)
    * findProductQuantityFor(cart, supplierCode)
    * findProductTotalFor(cart, supplierCode)
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

