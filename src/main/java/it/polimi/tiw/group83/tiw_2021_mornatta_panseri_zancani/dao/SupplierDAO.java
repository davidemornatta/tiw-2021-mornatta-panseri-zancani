package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.PriceRange;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Product;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private final Connection con;

    public SupplierDAO(Connection connection) {
        this.con = connection;
    }

    public Supplier createSupplierBean(ResultSet result) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setCode(result.getInt("code"));
        supplier.setName(result.getString("name"));
        supplier.setRating(result.getInt("rating"));
        supplier.setFreeShippingCost(result.getInt("free_shipping_cost"));

        return supplier;
    }

    private Supplier executeFindQuery(int code, String query) throws SQLException {
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, code);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return null;
                else {
                    result.next();
                    return createSupplierBean(result);
                }
            }
        }
    }

    public Supplier findSupplierByCode(int code) throws SQLException {
        String query = "SELECT  * FROM supplier  WHERE code = ?";
        return executeFindQuery(code, query);
    }

    public Supplier findMinPrice(int productCode) throws SQLException {
        String query = "SELECT  * " +
                "FROM supplier " +
                "WHERE code = (" +
                "SELECT supplier_code FROM sells WHERE product_code = ? AND price = (" +
                "SELECT MIN(price) FROM sells))";
        return executeFindQuery(productCode, query);
    }

    public int minPriceValue(int productCode) throws SQLException{
        String query = "SELECT TOP 1 price" +
                "FROM sells" +
                "WHERE product_code = ?" +
                "ORDER BY price ASC";

        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, productCode);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return 0;
                else {
                    result.next();
                    return result.getInt("price");
                }
            }
        }

    }

    public List<Supplier> findAllSuppliers(int productCode) throws SQLException {
        String query = "SELECT  * " +
                "FROM supplier " +
                "WHERE code = (" +
                "SELECT supplier_code FROM sells WHERE product_code = ?)";
        List<Supplier> suppliers = new ArrayList<>();
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, productCode);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return suppliers;
                else {
                    while (result.next()) {
                        suppliers.add(createSupplierBean(result));
                    }
                }
            }
        }
        return suppliers;
    }

    public int findProductsTotal(int supplierCode, List<Integer> productCodes) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT SUM(price) FROM sells WHERE supplier_code = ? AND product_code IN (");
        productCodes.forEach(code -> sb.append("?,"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        try(PreparedStatement preparedStatement = con.prepareStatement(sb.toString())) {
            for(int i = 0; i < productCodes.size(); i++) {
                preparedStatement.setInt(i + 1, productCodes.get(i));
            }
            try(ResultSet result = preparedStatement.executeQuery()) {
                if(!result.isBeforeFirst()) {
                    return 0;
                } else {
                    return result.getInt(1);
                }
            }
        }
    }
}
