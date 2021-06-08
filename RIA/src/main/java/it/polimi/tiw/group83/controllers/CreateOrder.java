package it.polimi.tiw.group83.controllers;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.OrderDAO;
import it.polimi.tiw.group83.dao.SupplierDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

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
import java.util.*;

@WebServlet("/CreateOrder")
@MultipartConfig
public class CreateOrder extends HttpServlet {
    private Connection connection;

    @Override
    public void init() throws ServletException {
        this.connection = ConnectionHandler.getConnection(this.getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        System.out.println(req.getParameter("supplier"));
        System.out.println(new String(Base64.getDecoder().decode(req.getParameter("cart"))));

        //TODO Implement

        Gson gson = new Gson();

        Date shippingDate = null;
        User user = (User) session.getAttribute("user");
        String order = req.getParameter("order");
        JsonObject jsonObject =gson.fromJson(order,JsonObject.class);
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        String supplierName;
        int supplierCode;

        float totalAmount = jsonObject.get("totalAmount").getAsFloat();


        supplierCode = jsonObject.get("supplierCode").getAsInt();
        Map<Integer,Integer> productList = new HashMap<>();
        JsonObject map = jsonObject.get("productList").getAsJsonObject();
        map.entrySet().forEach(entry -> {
            Integer keyInt = Integer.parseInt(entry.getKey());
            Integer valueInt = entry.getValue().getAsInt();
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
