package it.polimi.tiw.group83.tiw_2021_mornatta_panseri_zancani.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

public class TemplateUtils {
    public static TemplateEngine initTemplateEngine(ServletContext context) {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        return templateEngine;
    }
}
