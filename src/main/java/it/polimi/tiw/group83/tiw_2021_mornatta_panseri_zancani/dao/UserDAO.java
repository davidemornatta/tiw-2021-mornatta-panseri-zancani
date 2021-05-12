package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Product;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.User;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection con;

    public UserDAO(Connection connection) {
        this.con = connection;
    }

    public User checkCredentials(String usrn, String pwd) throws SQLException {
        String query = "SELECT  id, name, surname FROM user  WHERE name = ? AND password =?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setString(1, usrn);
            pstatement.setString(2, pwd);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no results, credential check failed
                    return null;
                else {
                    result.next();
                    User user = new User();
                    user.setId(result.getInt("id"));
                    user.setName(result.getString("name"));
                    user.setSurname(result.getString("surname"));
                    return user;
                }
            }
        }
    }

    public List<Product> findLastFiveViewedBy(int userId) throws SQLException, IOException {
        List<Product> products = new ArrayList<Product>();
        String query = "SELECT * from product JOIN recently_viewed ON code = product_code WHERE user_id = ?";
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setInt(1, userId);
            try (ResultSet result = pstatement.executeQuery();) {
                while (result.next()) {
                    Product product = new Product();
                    product.setCode(result.getInt("code"));
                    product.setName(result.getString("name"));
                    product.setDescription(result.getString("description"));
                    product.setCategory(result.getString("category"));
                    InputStream in = result.getBlob("image").getBinaryStream();
                    product.setImage(ImageIO.read(in));
                    products.add(product);
                }
            }
        }
      return products;
    }

    public void addViewToProductFrom(int userId, int productCode, java.sql.Date date) throws SQLException {
        int i;
        String query = "INSERT into recently_viewed (user_id, product_code, time) VALUES(?, ?, ?)";
        con.setAutoCommit(false);
        try (PreparedStatement pstatement = con.prepareStatement(query);) {
            pstatement.setInt(1, userId);
            pstatement.setInt(2, productCode);
            pstatement.setDate(3, date);
            i = pstatement.executeUpdate();
        }
        if(i == 0)
            throw  new SQLException();

        String delete = "DELETE FROM recently_viewed ORDER BY time DESC LIMIT 1";
        try (PreparedStatement p = con.prepareStatement(delete);){
            i=p.executeUpdate();
        }
        if(i==0){
            con.rollback();
            throw new SQLException();
        }
        con.commit();
    }
}
