package com.madirex.controller;

import com.madirex.exceptions.ClientException;
import com.madirex.models.funko.Funko;
import com.madirex.models.server.Login;
import com.madirex.models.server.Request;
import com.madirex.models.server.Response;
import com.madirex.services.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.madirex.models.server.Request.Type.*;

public class FunkoController {

    private static FunkoController funkoControllerInstance;
    private final Logger logger = LoggerFactory.getLogger(FunkoController.class);

    private FunkoController() {
    }

    public static FunkoController getInstance() {
        if (funkoControllerInstance == null) {
            funkoControllerInstance = new FunkoController();
        }
        return funkoControllerInstance;
    }

    public String sendRequestLogin() throws ClientException {
        String myToken = null;
        var loginJson = ServerConnection.getInstance().instanceToString(new Login("pepe", "pepe1234"));
        Request request = new Request(LOGIN, loginJson, null, LocalDateTime.now().toString());
        String str = "Petición enviada de tipo: " + LOGIN;
        logger.info(str);
        str = "Petición enviada: " + request;
        logger.debug(str);
        ServerConnection.getInstance().sendServerRequest(request);
        try {
            Response response = ServerConnection.getInstance().getServerData(Response.class);
            logger.debug("Respuesta recibida: " + response.toString());
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case TOKEN -> {
                    System.out.println("🟢 Mi token es: " + response.content());
                    myToken = response.content();
                }
                default -> throw new ClientException("Tipo de respuesta no esperado: " + response.content());

            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }
        return myToken;
    }

    private void sedRequestGetAllFunko(String token) throws IOException {
        Request request = new Request(GETALL, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETALL);
        logger.debug("Petición enviada: " + request);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                List<Funko> responseContent = ServerConnection.getInstance().getServerData((Class<List<Funko>>) (Class<?>) List.class);
                System.out.println("🟢 Los funko son: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: " + response.content());
        }
    }

    public void sendRequestGetFunkoById(String token, String id) throws ClientException, IOException {
        Request request = new Request(GETBYID, id, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETBYID);
        logger.debug("Petición enviada: " + request);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                Funko responseContent = ServerConnection.getInstance().getServerData(Funko.class);
                System.out.println("🟢 El funko solicitado es: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: Funko no encontrado con id: " + id + ". " + response.content());
            default -> throw new ClientException("Error no esperado al obtener el funko");
        }
    }

    private void sendRequestCreateFunko(String token, Funko funko) throws ClientException, IOException {
        var funkoJson = ServerConnection.getInstance().instanceToString(funko);
        Request request = new Request(INSERT, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + INSERT);
        logger.debug("Petición enviada: " + request);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                Funko responseContent = ServerConnection.getInstance().getServerData(Funko.class);
                System.out.println("🟢 El funko insertado es: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: No se ha podido insertar el funko: " + response.content());
            default -> throw new ClientException("Error no esperado al insertar el funko");
        }
    }

    public void sendRequestPutFunko(String token, Funko funko) throws ClientException, IOException {
        var funkoJson = ServerConnection.getInstance().instanceToString(funko);
        Request request = new Request(UPDATE, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + UPDATE);
        logger.debug("Petición enviada: " + request);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                Funko responseContent = ServerConnection.getInstance().getServerData(Funko.class);
                System.out.println("🟢 El funko actualizado es: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: No se ha podido actualizar el funko: " + response.content());
            default -> throw new ClientException("Error no esperado al actualizar el funko");
        }
    }

    public void sendRequestDeleteFunko(String token, String id) throws ClientException, IOException {
        Request request = new Request(DELETE, id, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + DELETE);
        logger.debug("Petición enviada: " + request);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseContent = ServerConnection.getInstance().getServerData(Funko.class);
                System.out.println("🟢 El funko eliminado es: " + responseContent);
            }
            case ERROR ->
                    System.err.println("🔴 Error: No se ha podido eliminar el funko con id: " + id + ". " + response.content());
            default -> throw new ClientException("Error no esperado al eliminar el funko");
        }
    }


    public void sendRequestExit(String token) throws ClientException, IOException {
        Request request = new Request(EXIT, null, token, LocalDateTime.now().toString());
        String str = "Petición enviada de tipo: " + EXIT;
        logger.info(str);
        str = "Petición enviada: " + request;
        logger.debug(str);
        ServerConnection.getInstance().sendServerRequest(request);
        Response response = ServerConnection.getInstance().getServerData(Response.class);
        str = "Respuesta recibida de tipo: " + response.status();
        logger.info(str);
        str = "Respuesta recibida: " + response;
        logger.debug(str);
        switch (response.status()) {
            case ERROR -> {
                str = "🔴 Error: " + response.content();
                logger.error(str);
            }
            case BYE -> {
                str = "Cerrando conexión " + response.content();
                logger.info(str);
                ServerConnection.getInstance().closeConnection();
            }
            default -> throw new ClientException(response.content());
        }
    }
}
