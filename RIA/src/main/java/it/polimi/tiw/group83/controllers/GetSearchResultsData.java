package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.util.LinkedHashMap;
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
        Map<Product, Float> products = new LinkedHashMap<>();
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

        Gson gson = new Gson();
        JsonArray array = new JsonArray();
        for (Map.Entry<Product, Float> entry : products.entrySet()) {
            JsonObject object = new JsonObject();
            object.add("product", gson.toJsonTree(entry.getKey()));
            object.addProperty("price", entry.getValue());
            array.add(object);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(array.toString());
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
