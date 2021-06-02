package it.polimi.tiw.group83.controllers;


import com.google.gson.Gson;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.OrderDAO;
import it.polimi.tiw.group83.dao.SupplierDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/CreateOrder")
@MultipartConfig
public class CreateOrder extends HttpServlet {
    private Connection connection;

    public CreateOrder() { }

    @Override
    public void init() throws ServletException {
        this.connection = ConnectionHandler.getConnection(this.getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        Gson gson = new Gson();

        Date shippingDate = null;
        User user = (User) session.getAttribute("user");
        String order = req.getParameter("order");
        JsonObject jsonObject =gson.fromJson(order,JsonObject.class);
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        String supplierName;
        int supplierCode = 0;

        JsonNumber totalAmountJson = jsonObject.getJsonNumber("totalAmount");
        float totalAmount = (float) totalAmountJson.doubleValue();


        supplierCode = jsonObject.getInt("supplierCode");
        Map<Integer,Integer> productList = new HashMap<>();
        JsonObject map = jsonObject.getJsonObject("productList");
        map.forEach((key,value)->{
            Integer keyInt = Integer.parseInt(key);
            Integer valueInt = value.asJsonObject().getInt("value");
            productList.put(keyInt,valueInt);
        });


        OrderDAO orderDAO = new OrderDAO(connection);
        try {
            orderDAO.createOrder(totalAmount ,
                    new java.sql.Date(Calendar.getInstance().getTime().getTime()),
                    user.getShippingAddress(),
                    supplierCode, user.getId(),
                    productList);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Unable to process order");
            return;
        }

        resp.setStatus(200);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(this.connection);
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

    }
}
