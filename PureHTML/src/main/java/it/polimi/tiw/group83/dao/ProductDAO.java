package it.polimi.tiw.group83.dao;

import it.polimi.tiw.group83.beans.Product;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProductDAO {
    private final Connection con;

    public ProductDAO(Connection connection) {
        this.con = connection;
    }

    public static Product createProductBean(ResultSet result) throws SQLException, IOException {
        Product product = new Product();
        product.setCode(result.getInt("code"));
        product.setName(result.getString("name"));
        product.setDescription(result.getString("description"));
        product.setCategory(result.getString("category"));
        InputStream in = result.getBlob("image").getBinaryStream();
        product.setImage(ImageIO.read(in));
        return product;
    }

    public Product findProductByCode(int code) throws SQLException, IOException {
        String query = "SELECT  * FROM product  WHERE code = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setInt(1, code);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return null;
                else {
                    result.next();
                    return createProductBean(result);
                }
            }
        }
    }

    public Map<Product, Float> searchForProductOrdered(String searchQuery) throws SQLException, IOException {
        String query = "SELECT code, name, description, category, image, min(price) AS 'price'  " +
                "FROM product JOIN sells ON code = product_code " +
                "WHERE name LIKE ? OR description LIKE ? OR category LIKE ? " +
                "GROUP BY code " +
                "ORDER BY price";
        Map<Product, Float> searchResult = new LinkedHashMap<>();
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setString(1, "%" + searchQuery + "%");
            pstatement.setString(2, "%" + searchQuery + "%");
            pstatement.setString(3, "%" + searchQuery + "%");
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return new HashMap<>();
                else {
                    while (result.next()) {
                        searchResult.put(createProductBean(result), result.getFloat("price"));
                    }
                }
            }
        }
        return searchResult;
    }

    public List<Product> findAllProductsByCodes(List<Integer> productCodes) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder("SELECT * FROM product WHERE code IN (");
        productCodes.forEach(code -> sb.append("?,"));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        List<Product> products = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(sb.toString())) {
            for (int i = 0; i < productCodes.size(); i++) {
                preparedStatement.setInt(i + 1, productCodes.get(i));
            }
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    products.add(createProductBean(result));
                }
            }
        }
        return products;
    }

    public boolean isProductSoldBy(int productCode, int supplierCode) throws SQLException {
        String query = "SELECT product_code FROM sells WHERE product_code = ? AND supplier_code = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, productCode);
            preparedStatement.setInt(2, supplierCode);
            try (ResultSet result = preparedStatement.executeQuery()) {
                return result.isBeforeFirst();
            }
        }
    }

    public float getProductPriceFor(int productCode, int supplierCode) throws SQLException {
        String query = "SELECT price FROM sells WHERE product_code = ? AND supplier_code = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, productCode);
            preparedStatement.setInt(2, supplierCode);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (!result.isBeforeFirst()) {
                    return -1;
                } else {
                    result.next();
                    return result.getFloat("price");
                }
            }
        }
    }

    public Collection<? extends Product> findRandomProducts(int quantity) throws SQLException, IOException {
        String query = "SELECT  * FROM product WHERE category = 'Technology' LIMIT ?";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setInt(1, quantity);

            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    products.add(createProductBean(result));
                }
            }
        }
        return products;
    }
}
