package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Product;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final Connection con;

    public ProductDAO(Connection connection) {
        this.con = connection;
    }

    public static Product createProductBean(ResultSet result) throws SQLException, IOException{
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

    public List<Product> searchForProduct(String searchQuery) throws SQLException, IOException{
        String query = "SELECT  * FROM product" +
                " WHERE name LIKE '%?%' OR description LIKE '%?%' OR category LIKE '%?%'";
        List<Product> searchResult = new ArrayList<>();
        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            pstatement.setString(1, searchQuery);
            pstatement.setString(2, searchQuery);
            pstatement.setString(3, searchQuery);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst()) // no results, product not found
                    return searchResult;
                else {
                    while(result.next()) {
                        searchResult.add(createProductBean(result));
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
        try(PreparedStatement preparedStatement = con.prepareStatement(sb.toString())) {
            for(int i = 0; i < productCodes.size(); i++) {
                preparedStatement.setInt(i + 1, productCodes.get(i));
            }
            try(ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    products.add(createProductBean(result));
                }
            }
        }
        return products;
    }
}
