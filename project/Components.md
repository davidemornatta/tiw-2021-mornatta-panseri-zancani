Components
•	Model objects (Beans)
o	User
o	Product
o	Supplier
o	PriceRange
o	Order
•	Data Access objects (Classes)
o	UserDAO
	checkCredentials(username, password)
o	ProductDAO
	findProductByCode(code)
	searchForProduct(searchQuery)
o	SupplierDAO
	findSupplierByCode(code)
o	PriceRangeDAO
	findPriceRangesForSupplier(supplierCode)
o	OrderDAO
	createOrder(order, userId)
	findUserOrders(userId)
o	RecentlyViewedDAO
	findLastFiveViewedBy(userId)
	addViewToProductFrom(userId, productCode)
o	OrderContainsDAO
	findAllProductsInOrder(orderCode)
o	SellsDAO
	findMinPrice(productCode)
	findAllSuppliers(productCode)
	findProductQuantityFor(cart, supplierCode)
	findProductTotalFor(cart, supplierCode)
•	Controllers (Servlets)
o	CheckLogin
o	GoToHome
o	GoToOrders
o	GoToShoppingCart
o	GoToSearchResults
o	UpdateCart
o	ProcessOrder
•	Views (Templates)
o	LoginPage
o	Home
o	Navbar
o	Orders
o	SearchResults
o	ShoppingCart

