package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.controllers;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Cart;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.User;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.OrderDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.ConnectionHandler;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.TemplateUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

@WebServlet("/ProcessOrder")
public class ProcessOrder extends HttpServlet {
    private Connection connection;

    public ProcessOrder() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        Cart cart = (Cart) session.getAttribute("cart");
        User user = (User) session.getAttribute("user");
        String supplierCodeRaw = req.getParameter("supplier");
        int supplierCode = 0;
        try {
            if(supplierCodeRaw == null)
                throw new RuntimeException();
            supplierCode = Integer.parseInt(supplierCodeRaw);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No supplier code given");
            return;
        }
        OrderDAO orderDAO = new OrderDAO(connection);
        try {
            orderDAO.createOrder(cart.findProductTotalFor(supplierCode, connection),
                    new java.sql.Date(Calendar.getInstance().getTime().getTime()),
                    user.getShippingAddress(),
                    supplierCode, user.getId(),
                    cart.findAllProductAndQuantitiesFor(supplierCode));
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process order");
            return;
        }

        cart.removeOrderedItems(supplierCode);

        String path = getServletContext().getContextPath() + "/GoToOrders";
        resp.sendRedirect(path);
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
