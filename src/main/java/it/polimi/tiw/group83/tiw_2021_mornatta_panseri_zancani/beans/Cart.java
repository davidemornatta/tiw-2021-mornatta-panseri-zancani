package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.ProductDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.SupplierDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Cart {
    private final Map<Integer, List<Integer>> supplierProductsMap;

    public Cart() {
        supplierProductsMap = new HashMap<>();
    }

    public int findProductQuantityFor(int supplierCode) {
        return supplierProductsMap.getOrDefault(supplierCode, Collections.emptyList()).size();
    }

    public int findProductTotalFor(int supplierCode, Connection connection) throws SQLException {
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        return supplierDAO.findProductsTotal(supplierCode, supplierProductsMap.get(supplierCode));
    }

    private List<Product> findAllProductsFor(int supplierCode, Connection connection) throws IOException, SQLException {
        ProductDAO productDAO = new ProductDAO(connection);
        return productDAO.findAllProductsByCodes(supplierProductsMap.get(supplierCode));
    }

    public Map<String, List<Product>> findAllProducts(Connection connection) throws SQLException, IOException {
        Map<String, List<Product>> result = new HashMap<>();
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        for(int supplierCode : supplierProductsMap.keySet()) {
            Supplier supplier = supplierDAO.findSupplierByCode(supplierCode);
            List<Product> products = findAllProductsFor(supplierCode, connection);
            result.put(supplier.getName(), products);
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

    public Map<Integer, Integer> findAllProductAndQuantitiesFor(int supplierCode) {
        Map<Integer, Integer> result = new HashMap<>();
        List<Integer> supplierProductsCopy = new ArrayList<>(supplierProductsMap.get(supplierCode));
        for (int i = supplierProductsCopy.size() - 1; i >= 0; i--) {
            int quantity = 1;
            for (int j = supplierProductsCopy.size()-2 ; j >= 0; j--) {
                if (supplierProductsCopy.get(i).equals(supplierProductsCopy.get(j))) {
                    quantity++;
                    supplierProductsCopy.remove(j);
                }
            }
            result.put(supplierProductsCopy.get(i), quantity);
            supplierProductsCopy.remove(i);
        }
        return result;
    }

    public void removeOrderedItems(int supplier) {
        supplierProductsMap.remove(supplier);
    }

    public void addProduct(int supplierCode, int productCode) {
        if(supplierProductsMap.containsKey(supplierCode))
            supplierProductsMap.get(supplierCode).add(productCode);
        else
            supplierProductsMap.put(supplierCode, new ArrayList<>(Collections.singletonList(productCode)));
    }
}
