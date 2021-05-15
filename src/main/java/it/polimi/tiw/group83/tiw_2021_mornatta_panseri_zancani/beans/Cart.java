package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.ProductDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.SupplierDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Product> findAllProductsFor(int supplierCode, Connection connection) throws IOException, SQLException {
        ProductDAO productDAO = new ProductDAO(connection);
        return productDAO.findAllProductsByCodes(supplierProductsMap.get(supplierCode));
    }
}
