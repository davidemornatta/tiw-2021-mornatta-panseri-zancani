package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.controllers;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.Cart;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.ProductDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.SupplierDAO;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

@WebServlet("/UpdateCart")
public class UpdateCart extends HttpServlet {
    private Connection connection;

    public UpdateCart() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if(req.getParameter("productCode") == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing productCode parameter");
            return;
        }
        if(req.getParameter("supplierCode") == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing supplierCode parameter");
            return;
        }

        int productCode;
        try {
            productCode = Integer.parseInt(req.getParameter("productCode"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productCode parameter");
            return;
        }
        ProductDAO productDAO = new ProductDAO(connection);
        try {
            if(productDAO.findProductByCode(productCode) == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productCode parameter");
                return;
            }
        } catch (SQLException | IOException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Db error");
            return;
        }

        int supplierCode;
        try {
            supplierCode = Integer.parseInt(req.getParameter("supplierCode"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid productCode parameter");
            return;
        }
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        try {
            if(supplierDAO.findSupplierByCode(supplierCode) == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid supplierCode parameter");
                return;
            }
        } catch (SQLException | IOException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Db error");
            return;
        }

        try {
            if(supplierDAO.findProductsTotal(supplierCode, Collections.singletonList(productCode)) == -1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Supplier does not sell product");
                return;
            }
        } catch (SQLException | IOException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Db error");
            return;
        }


        Cart cart = (Cart) session.getAttribute("cart");
        cart.addProduct(supplierCode, productCode);

        String path = getServletContext().getContextPath() + "/GoToShoppingCart";
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
