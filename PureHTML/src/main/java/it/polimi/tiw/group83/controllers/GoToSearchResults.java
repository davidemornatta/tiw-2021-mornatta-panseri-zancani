package it.polimi.tiw.group83.controllers;

import it.polimi.tiw.group83.beans.*;
import it.polimi.tiw.group83.dao.PriceRangeDAO;
import it.polimi.tiw.group83.dao.ProductDAO;
import it.polimi.tiw.group83.dao.SupplierDAO;
import it.polimi.tiw.group83.dao.UserDAO;
import it.polimi.tiw.group83.beans.*;
import it.polimi.tiw.group83.dao.*;
import it.polimi.tiw.group83.utils.ConnectionHandler;
import it.polimi.tiw.group83.utils.TemplateUtils;
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
import java.util.*;

@WebServlet("/GoToSearchResults")
public class GoToSearchResults extends HttpServlet {
    private Connection connection;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        templateEngine = TemplateUtils.initTemplateEngine(getServletContext());
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        ProductDAO productDAO = new ProductDAO(connection);
        String path = "/WEB-INF/search.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        String searchQuery = request.getParameter("searchQuery");
        Map<Product, Float> products = new HashMap<>();
        if (searchQuery != null && !searchQuery.isEmpty()) {
            try {
                products = productDAO.searchForProductOrdered(searchQuery);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        ctx.setVariable("products", products);

        String selectedCode = request.getParameter("selectedCode");
        boolean isProductSelected = false;
        if(selectedCode != null && !selectedCode.isEmpty()) {
            int productCode;
            try {
                productCode = Integer.parseInt(selectedCode);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Given selectedCode is not a number");
                return;
            }

            Product selectedProduct;
            try {
                selectedProduct = productDAO.findProductByCode(productCode);
                if(selectedProduct == null)
                    throw new RuntimeException();
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot find product with given code");
                return;
            }

            List<Supplier> suppliers;
            Map<Supplier, Float> supplierPrice = new HashMap<>();
            Map<Supplier, List<PriceRange>> ranges = new HashMap<>();
            Map<Supplier, Integer> totalQuantity = new HashMap<>();
            Map<Supplier, Float> totalAmount = new HashMap<>();
            Cart cart = (Cart) session.getAttribute("cart");

            SupplierDAO supplierDAO = new SupplierDAO(connection);
            try {
                suppliers = supplierDAO.findAllSuppliers(productCode);

                PriceRangeDAO priceRangeDAO = new PriceRangeDAO(connection);
                for (Supplier s : suppliers) {
                    float price = productDAO.getProductPriceFor(productCode, s.getCode());
                    supplierPrice.put(s, price);

                    int quantity = cart.findProductQuantityFor(s.getCode());
                    totalQuantity.put(s, quantity);

                    float total = cart.findProductTotalFor(s.getCode(), connection);
                    totalAmount.put(s, total);

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

            ctx.setVariable("selectedProduct", selectedProduct);
            ctx.setVariable("supplierPrice", supplierPrice);
            ctx.setVariable("supplierRanges", ranges);
            ctx.setVariable("supplierQuantity", totalQuantity);
            ctx.setVariable("supplierTot", totalAmount);

            isProductSelected = true;
        }
        ctx.setVariable("isProductSelected", isProductSelected);

        templateEngine.process(path, ctx, response.getWriter());
    }

}
