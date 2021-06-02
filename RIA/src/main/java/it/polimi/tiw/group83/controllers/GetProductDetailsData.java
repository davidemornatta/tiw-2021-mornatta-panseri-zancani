package it.polimi.tiw.group83.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.tiw.group83.beans.PriceRange;
import it.polimi.tiw.group83.beans.Product;
import it.polimi.tiw.group83.beans.Supplier;
import it.polimi.tiw.group83.beans.User;
import it.polimi.tiw.group83.dao.PriceRangeDAO;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.dao.SupplierDAO;
import it.polimi.tiw.group83.dao.UserDAO;
import it.polimi.tiw.group83.utils.ConnectionHandler;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GetProductDetailsData")
public class GetProductDetailsData extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public GetProductDetailsData() {
        super();
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        int productCode;
        try {
            productCode = Integer.parseInt(request.getParameter("productCode"));
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect param values");
            return;
        }

        ProductDAO productDAO = new ProductDAO(connection);
        Product selectedProduct;
        try {
            selectedProduct = productDAO.findProductByCode(productCode);
            if (selectedProduct == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().println("Resource not found");
                return;
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to recover mission");
            return;
        }

        List<Supplier> suppliers;
        Map<Supplier, Float> supplierPrice = new HashMap<>();
        Map<Supplier, List<PriceRange>> ranges = new HashMap<>();

        SupplierDAO supplierDAO = new SupplierDAO(connection);
        try {
            suppliers = supplierDAO.findAllSuppliers(productCode);

            PriceRangeDAO priceRangeDAO = new PriceRangeDAO(connection);
            for (Supplier s : suppliers) {
                float price = productDAO.getProductPriceFor(productCode, s.getCode());
                supplierPrice.put(s, price);

                List<PriceRange> priceRanges = priceRangeDAO.findPriceRangesForSupplier(s.getCode());
                ranges.put(s, priceRanges);
            }

            UserDAO userDAO = new UserDAO(connection);
            userDAO.addViewToProductFrom(user.getId(), productCode, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load product details");
            return;
        }

        String jsonProduct = new Gson().toJson(selectedProduct);
        String jsonSuppliers = new Gson().toJson(suppliers);
        String jsonSuppliersPrice = new Gson().toJson(supplierPrice);
        String jsonSuppliersRanges = new Gson().toJson(ranges);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("product", jsonProduct);
        jsonObject.addProperty("suppliers", jsonSuppliers);
        jsonObject.addProperty("supplierPrice", jsonSuppliersPrice);
        jsonObject.addProperty("ranges", jsonSuppliersRanges);
        String json = new Gson().toJson(jsonObject);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}