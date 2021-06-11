package it.polimi.tiw.group83.dao;

import it.polimi.tiw.group83.beans.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public Supplier findSupplierByCode(int code) throws SQLException {
        String query = "SELECT  * FROM supplier  WHERE code = ?";
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

    public List<Supplier> findAllSuppliersFor(int productCode) throws SQLException {
        String query = "SELECT  * " +
                "FROM supplier " +
                "WHERE code IN (" +
                "SELECT supplier_code FROM sells WHERE product_code = ?)";
        List<Supplier> suppliers = new ArrayList<>();
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, productCode);
            try (ResultSet result = pstatement.executeQuery()) {
                if (result.isBeforeFirst()) {
                    while (result.next()) {
                        suppliers.add(createSupplierBean(result));
                    }
                }
            }
        }
        return suppliers;
    }

    public float findProductsTotalWithQuantities(int supplierCode, Map<Integer, Integer> productCodesAndQuantities) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT product_code, price FROM sells WHERE supplier_code = ? AND product_code IN (");
        productCodesAndQuantities.keySet().forEach(code -> sb.append("?,"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        try (PreparedStatement preparedStatement = con.prepareStatement(sb.toString())) {
            preparedStatement.setInt(1, supplierCode);
            int i = 2;
            for (int productCode : productCodesAndQuantities.keySet()) {
                preparedStatement.setInt(i, productCode);
                i++;
            }
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) {
                    return -1;
                } else {
                    float total = 0;
                    while (result.next()) {
                        int productCode = result.getInt(1);
                        float price = result.getFloat(2);
                        total += price * productCodesAndQuantities.get(productCode);
                    }
                    return total;
                }
            }
        }
    }
}
