package it.polimi.tiw.group83.beans;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.tiw.group83.dao.PriceRangeDAO;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.dao.SupplierDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Cart {
    private final Map<Integer, Map<Integer, Integer>> supplierProductsMap;

    public Cart(Map<Integer, Map<Integer, Integer>> supplierProductsMap) {
        this.supplierProductsMap = supplierProductsMap;
    }

    public int findProductQuantityFor(int supplierCode) {
        int totQuantity = 0;
        if (supplierProductsMap.containsKey(supplierCode)) {
            for (int quantity : supplierProductsMap.get(supplierCode).values()) {
                totQuantity += quantity;
            }
        }
        return totQuantity;
    }

    public float findProductTotalFor(int supplierCode, Connection connection) throws SQLException {
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        float total = 0;
        if (supplierProductsMap.containsKey(supplierCode)) {
            total = supplierDAO.findProductsTotalWithQuantities(supplierCode, supplierProductsMap.get(supplierCode));
        }
        return total != -1 ? total : 0;
    }

    private List<Product> findAllProductsFor(int supplierCode, Connection connection) throws IOException, SQLException {
        ProductDAO productDAO = new ProductDAO(connection);
        return productDAO.findAllProductsByCodes(new ArrayList<>(supplierProductsMap.get(supplierCode).keySet()));
    }

    public Map<String, Map<Product, Integer>> findAllProducts(Connection connection) throws SQLException, IOException {
        Map<String, Map<Product, Integer>> result = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for (int supplierCode : supplierProductsMap.keySet()) {
            Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
            List<Product> products = findAllProductsFor(supplierCode, connection);
            Map<Product, Integer> productQuantities = new HashMap<>();
            for (Product product : products)
                productQuantities.put(product, supplierProductsMap.get(supplierCode).get(product.getCode()));
            result.put(supplier.getName(), productQuantities);
        }
        return result;
    }

    public Map<String, Integer> getAllSupplierCodes(Connection connection) throws SQLException {
        Map<String, Integer> supplierCodes = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for (int supplierCode : supplierProductsMap.keySet()) {
            String name = supplierDAO.findSupplierByCode(supplierCode).getName();
            supplierCodes.put(name, supplierCode);
        }
        return supplierCodes;
    }

    public Map<String, Float> findAllProductTotals(Connection connection) throws SQLException {
        Map<String, Float> result = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for (int supplierCode : supplierProductsMap.keySet()) {
            Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
            float total = findProductTotalFor(supplierCode, connection);
            result.put(supplier.getName(), total);
        }
        return result;
    }


    private Map.Entry<String, Float> calculateShippingTotal(int supplierCode, Connection con) throws SQLException {
        float shippingCosts = 0;
        float supplierTotalAmount = findProductTotalFor(supplierCode, con);
        SupplierDAO supplierDAO = new SupplierDAO(con);
        Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
        float minForFreeShipping = supplier.getFreeShippingCost();

        if (supplierTotalAmount <= minForFreeShipping) {
            PriceRangeDAO prDAO = new PriceRangeDAO(con);
            List<PriceRange> priceRanges = prDAO.findPriceRangesForSupplier(supplierCode);
            int productQuantity = findProductQuantityFor(supplierCode);
            for (PriceRange pr : priceRanges) {
                if (productQuantity >= pr.getMinArticles() && productQuantity <= pr.getMaxArticles()) {
                    shippingCosts = pr.getShippingCost();
                    break;
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(supplier.getName(), shippingCosts);
    }

    public void checkValidity(Connection con) throws SQLException {
        ProductDAO productDAO = new ProductDAO(con);
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : supplierProductsMap.entrySet()) {
            Integer supplier = entry.getKey();
            Map<Integer, Integer> productQuantities = entry.getValue();
            Iterator<Integer> iterator = productQuantities.keySet().iterator();
            int productCode;
            while (iterator.hasNext()) {
                productCode = iterator.next();
                if (!productDAO.isProductSoldBy(productCode, supplier))
                    iterator.remove();
            }
        }
    }

    public Map<String, Float> getAllShippingCosts(Connection con) throws SQLException {
        Map<String, Float> shippingCosts = new HashMap<>();

        for (int supplierCode : supplierProductsMap.keySet()) {
            Map.Entry<String, Float> entry = calculateShippingTotal(supplierCode, con);
            shippingCosts.put(entry.getKey(), entry.getValue());
        }

        return shippingCosts;
    }

    public Map<Integer, Integer> findAllProductAndQuantitiesFor(int supplierCode) {
        return new HashMap<>(supplierProductsMap.get(supplierCode));
    }

    public static Cart loadFromBase64(String base64) throws RuntimeException {
        if (base64 == null)
            throw new IllegalArgumentException("Base64 cart can't be null");

        String jsonCart = new String(Base64.getDecoder().decode(base64));

        Map<Integer, Map<Integer, Integer>> cartContents = new HashMap<>();
        JsonObject cartObj = JsonParser.parseString(jsonCart).getAsJsonObject();
        for (String supplierCodeString : cartObj.keySet()) {
            int supplierCode = Integer.parseInt(supplierCodeString);
            JsonObject supplierProductsObj = cartObj.getAsJsonObject(supplierCodeString);
            HashMap<Integer, Integer> productQuantities = new HashMap<>();
            for (String productCodeString : supplierProductsObj.keySet()) {
                int productCode = Integer.parseInt(productCodeString);
                int quantity = supplierProductsObj.get(productCodeString).getAsInt();
                productQuantities.put(productCode, quantity);
            }
            cartContents.put(supplierCode, productQuantities);
        }
        return new Cart(cartContents);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "supplierProductsMap=" + supplierProductsMap +
                '}';
    }
}
