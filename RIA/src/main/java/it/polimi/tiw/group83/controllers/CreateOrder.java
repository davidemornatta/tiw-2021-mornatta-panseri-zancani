package it.polimi.tiw.group83.controllers;


import it.polimi.tiw.group83.beans.Cart;
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
import java.util.Calendar;

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
        User user = (User) session.getAttribute("user");

        Cart cart;
        try {
            cart = Cart.loadFromBase64(req.getParameter("cart"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Malformed cart parameter!");
            return;
        }

        String supplierCodeRaw = req.getParameter("supplier");
        int supplierCode;
        try {
            supplierCode = Integer.parseInt(supplierCodeRaw);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("No supplier code given");
            return;
        }

        SupplierDAO supplierDAO = new SupplierDAO(connection);
        String supplierName;
        OrderDAO orderDAO = new OrderDAO(connection);
        try {
            supplierName = supplierDAO.findSupplierByCode(supplierCode).getName();
            orderDAO.createOrder(cart.findProductTotalFor(supplierCode, connection) + cart.getAllShippingCosts(connection).get(supplierName),
                    new java.sql.Date(Calendar.getInstance().getTime().getTime()),
                    user.getShippingAddress(),
                    supplierCode, user.getId(),
                    cart.findAllProductAndQuantitiesFor(supplierCode));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Unable to process order");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(this.connection);
        } catch (SQLException var2) {
            var2.printStackTrace();
        }
    }
}
