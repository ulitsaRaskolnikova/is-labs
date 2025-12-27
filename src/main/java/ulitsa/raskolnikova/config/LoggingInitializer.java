package ulitsa.raskolnikova.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@WebListener
public class LoggingInitializer implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(LoggingInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InputStream loggingProperties = getClass().getClassLoader()
                .getResourceAsStream("logging.properties");
            if (loggingProperties != null) {
                LogManager.getLogManager().readConfiguration(loggingProperties);
                System.out.println("[LoggingInitializer] Logging configuration loaded from logging.properties");
                logger.info("Logging configuration loaded from logging.properties");
            } else {
                System.out.println("[LoggingInitializer] WARNING: logging.properties not found, using default logging configuration");
                logger.warning("logging.properties not found, using default logging configuration");
            }
        } catch (Exception e) {
            System.err.println("[LoggingInitializer] ERROR: Failed to load logging.properties: " + e.getMessage());
            e.printStackTrace();
            logger.severe("Failed to load logging.properties: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}

