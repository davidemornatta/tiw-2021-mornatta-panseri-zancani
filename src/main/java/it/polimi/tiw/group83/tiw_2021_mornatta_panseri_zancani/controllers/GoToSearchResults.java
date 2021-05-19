package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.controllers;

import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.beans.*;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.dao.*;
import it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

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
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        int selectedCode = Integer.parseInt(null);
        String searchQuery = request.getParameter("searchQuery");
        try {
             selectedCode = Integer.parseInt(request.getParameter("selectedCode"));
        }catch(Exception e){

        }
        ProductDAO productDAO = new ProductDAO(connection);
        Map<Product,Integer> products = new HashMap<>();
        List<Supplier> suppliers= new ArrayList<>();
        Map<Supplier,List<PriceRange>> ranges = new HashMap<>();
        Map<Supplier, Integer> totalQuantity = new HashMap<>();
        Map<Supplier, Integer> totalAmount = new HashMap<>();
        Cart cart = (Cart)session.getAttribute("cart");


        if (searchQuery != null || searchQuery.isEmpty()) {
            try {
                throw new Exception("Missing or empty credential value");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                products = productDAO.searchForProductOrdered(searchQuery);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        if(selectedCode!=0){
            SupplierDAO supplierDAO = new SupplierDAO(connection);
            try {
                suppliers = supplierDAO.findAllSuppliers(selectedCode);
                PriceRangeDAO priceRangeDAO = new PriceRangeDAO(connection);
                for(Supplier s: suppliers){
                    int quantity = cart.findProductQuantityFor(s.getCode());
                    //Serve metodo in cart
                   // int total = cart.findProductTotalFor(s.getCode(),connection);
                    List<PriceRange> priceRanges= priceRangeDAO.findPriceRangesForSupplier(s.getCode());
                    totalQuantity.put(s,quantity);
                   // totalAmount.put(s,total);
                    ranges.put(s,priceRanges);
                }
                UserDAO userDAO= new UserDAO(connection);
                userDAO.addViewToProductFrom(user.getId(), selectedCode, (java.sql.Date) new Date(System.currentTimeMillis()));


            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }

        String path = "/WEB-INF/SearchResults.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("products", products);
        ctx.setVariable("supplierRanges",ranges);
        ctx.setVariable("supplierQuantity",totalQuantity);
        ctx.setVariable("supplierTot",totalAmount);
        templateEngine.process(path, ctx, response.getWriter());
//    }


    }

}
