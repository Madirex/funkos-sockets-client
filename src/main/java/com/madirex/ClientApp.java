package com.madirex;

import com.madirex.service.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Clase ClientApp
 */
public class ClientApp {
    private static ClientApp clientAppInstance;
    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);

    /**
     * Constructor de la clase
     */
    private ClientApp() {
    }

    /**
     * Devuelve la instancia de la clase
     *
     * @return Instancia de la clase
     */
    public static ClientApp getInstance() {
        if (clientAppInstance == null) {
            clientAppInstance = new ClientApp();
        }
        return clientAppInstance;
    }

    /**
     * Ejecuta la aplicación
     */
    public void run() {
        ServerConnection serverConnection = new ServerConnection();
        try {
            serverConnection.start();
        } catch (IOException e) {
            String str = "Error: " + e.getMessage();
            logger.error(str);
        }
    }
}
