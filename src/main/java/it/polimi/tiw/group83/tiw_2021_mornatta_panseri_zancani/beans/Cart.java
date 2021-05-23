package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.ProductDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.SupplierDAO;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Cart {
    private final Map<Integer, Map<Integer, Integer>> supplierProductsMap;

    public Cart() {
        supplierProductsMap = new HashMap<>();
    }

    public int findProductQuantityFor(int supplierCode) {
        int totQuantity = 0;
        if(supplierProductsMap.containsKey(supplierCode)) {
            for(int quantity : supplierProductsMap.get(supplierCode).values()) {
                totQuantity += quantity;
            }
        }
        return totQuantity;
    }

    public int findProductTotalFor(int supplierCode, Connection connection) throws SQLException {
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        int total = 0;
        if(supplierProductsMap.containsKey(supplierCode)) {
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
        for(int supplierCode : supplierProductsMap.keySet()) {
            Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
            List<Product> products = findAllProductsFor(supplierCode, connection);
            Map<Product, Integer> productQuantities = new HashMap<>();
            for(Product product : products)
                productQuantities.put(product, supplierProductsMap.get(supplierCode).get(product.getCode()));
            result.put(supplier.getName(), productQuantities);
        }
        return result;
    }

    public Map<String, Integer> findAllProductTotals(Connection connection) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for(int supplierCode : supplierProductsMap.keySet()) {
            Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
            int total = findProductTotalFor(supplierCode, connection);
            result.put(supplier.getName(), total);
        }
        return result;
    }

    public Map<String, Integer> getAllSupplierCodes(Connection connection) throws SQLException {
        Map<String, Integer> supplierCodes = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for(int supplierCode : supplierProductsMap.keySet()) {
            String name = supplierDAO.findSupplierByCode(supplierCode).getName();
            supplierCodes.put(name, supplierCode);
        }
        return supplierCodes;
    }

    public Map<Integer, Integer> findAllProductAndQuantitiesFor(int supplierCode) {
        return new HashMap<>(supplierProductsMap.get(supplierCode));
    }

    public void removeOrderedItems(int supplier) {
        supplierProductsMap.remove(supplier);
    }

    public void addProduct(int supplierCode, int productCode, int quantity) {
        if(supplierProductsMap.containsKey(supplierCode))
            if(supplierProductsMap.get(supplierCode).containsKey(productCode)) {
                int prevQuantity = supplierProductsMap.get(supplierCode).get(productCode);
                supplierProductsMap.get(supplierCode).put(productCode, quantity + prevQuantity);
            } else
                supplierProductsMap.get(supplierCode).put(productCode, quantity);
        else
            supplierProductsMap.put(supplierCode, new HashMap<>(Map.of(productCode, quantity)));
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
}
