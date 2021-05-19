package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Order;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Product;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private final Connection con;

    public OrderDAO(Connection con) {
        this.con = con;
    }

    public void createOrder(float totalAmount, Date shippingDate, String shippingAddress, int supplierCode, int userId,
                            Map<Integer, Integer> productList) throws SQLException {
        con.setAutoCommit(false);
        String query = "INSERT INTO 'order' (total_amount, shipping_date, shipping_address, supplier_code, user_id) VALUES (?, ?, ?, ?, ?)";
        int generatedCode;
        try(PreparedStatement preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setFloat(1, totalAmount);
            preparedStatement.setDate(2, shippingDate);
            preparedStatement.setString(3, shippingAddress);
            preparedStatement.setInt(4, supplierCode);
            preparedStatement.setInt(5, userId);

            int inserted = preparedStatement.executeUpdate();
            if(inserted == 0) {
                con.rollback();
                throw new SQLException();
            }

            ResultSet result = preparedStatement.getGeneratedKeys();
            result.next();
            generatedCode = result.getInt("code");
        }

        StringBuilder sb = new StringBuilder("INSERT INTO order_contains (order_code, product_code, quantity) VALUES ");
        productList.keySet().forEach(key -> sb.append("(?, ?, ?),"));
        sb.deleteCharAt(sb.length() - 1);
        try(PreparedStatement preparedStatement = con.prepareStatement(sb.toString())) {
            int index = 1;
            for(int productCode : productList.keySet()) {
                preparedStatement.setInt(index, generatedCode);
                preparedStatement.setInt(index + 1, productCode);
                preparedStatement.setInt(index + 2, productList.get(productCode));
                index += 3;
            }

            int inserted = preparedStatement.executeUpdate();
            if(inserted == 0) {
                con.rollback();
                throw new SQLException();
            }
        }
        con.commit();
        con.setAutoCommit(true);
    }

    public List<Order> findUserOrders(int userId) throws SQLException {
        String query = "SELECT * FROM `order` WHERE user_id = ? ORDER BY shipping_date DESC";
        List<Order> orders = new ArrayList<>();
        try(PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);

            try(ResultSet result = preparedStatement.executeQuery()) {
                if(!result.isBeforeFirst())
                    return orders;
                else {
                    while (result.next()) {
                        Order order = new Order();
                        order.setCode(result.getInt("code"));
                        order.setTotalAmount(result.getFloat("total_amount"));
                        order.setShippingDate(result.getDate("shipping_date"));
                        order.setShippingAddress(result.getString("shipping_address"));
                        order.setSupplierCode(result.getInt("supplier_code"));
                        order.setUserId(result.getInt("user_id"));
                        orders.add(order);
                    }
                }
            }
        }
        return orders;
    }

    public Map<Product, Integer> findAllProductsInOrder(int orderCode) throws SQLException, IOException {
        String query = "SELECT * FROM order_contains JOIN product p on p.code = order_contains.product_code WHERE order_code = ?";
        Map<Product, Integer> products = new HashMap<>();
        try(PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, orderCode);

            try(ResultSet result = preparedStatement.executeQuery()) {
                if(!result.isBeforeFirst())
                    return products;
                else {
                    while (result.next()) {
                        products.put(ProductDAO.createProductBean(result), result.getInt("quantity"));
                    }
                }
            }
        }
        return products;
    }
}
