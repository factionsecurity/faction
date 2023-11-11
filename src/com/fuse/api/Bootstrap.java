package com.fuse.api;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.BeanConfig;

public class Bootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.1.0");
        beanConfig.setBasePath(this.getServletContext().getContextPath() + "/api");
        beanConfig.setResourcePackage("com.fuse.api");
        beanConfig.setScan(true);
    }
}