package com.madirex;

import com.madirex.controller.FunkoController;
import com.madirex.exceptions.ClientException;
import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.services.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Clase ClientApp
 */
public class ClientApp {
    private static ClientApp clientAppInstance;
    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);
    private final FunkoController funkoController = FunkoController.getInstance();

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
        try {
            ServerConnection.getInstance().openConnection();
            String token = funkoController.sendRequestLogin();
            //funkoController.sendRequestGetAllFunko(token);
            var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
            //funkoController.sendRequestInsertFunko(token, funko);
            funkoController.sendRequestGetFunkoById(token, "1");
            //funkoController.sendRequestGetAllFunko(token);
            funko = Funko.builder().name("cuackupdated").price(42.23).releaseDate(LocalDate.now()).model(Model.MARVEL).build();
            funkoController.sendRequestPutFunko(token, funko);
            funkoController.sendRequestDeleteFunko(token, "1");
            //funkoController.sendRequestGetAllFunko(token);
            funkoController.sendRequestPutFunko(token, funko);
            funkoController.sendRequestDeleteFunko(token, "1");
            //funkoController.sendRequestGetAllFunko(token);
            funkoController.sendRequestExit(token);
        } catch (ClientException ex) {
            logger.error("🔴 Error: " + ex.getMessage());
            ServerConnection.getInstance().closeConnection();
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
