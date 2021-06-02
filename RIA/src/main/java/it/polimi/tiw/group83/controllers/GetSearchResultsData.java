package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/GetSearchResultsData")
public class GetSearchResultsData extends HttpServlet {
    private Connection connection;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProductDAO productDAO = new ProductDAO(connection);
        String searchQuery = request.getParameter("searchQuery");
        Map<Product, Float> products = new HashMap<>();
        if (searchQuery != null && !searchQuery.isEmpty()) {
            try {
                products = productDAO.searchForProductOrdered(searchQuery);
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Db error, retry later");
                return;
            }
        }

        String json = new Gson().toJson(products);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
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
