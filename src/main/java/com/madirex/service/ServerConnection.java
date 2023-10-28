package com.madirex.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.madirex.exceptions.ClientException;
import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.models.server.Login;
import com.madirex.models.server.Request;
import com.madirex.models.server.Response;
import com.madirex.utils.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.madirex.models.server.Request.Type.*;

public class ServerConnection {
    public static final String KEY_FILE_CONFIG_NAME = "keyFile";
    public static final String KEY_PASSWORD_CONFIG_NAME = "keyPassword";
    private static int port;
    private static String host;
    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.madirex.utils.LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new com.madirex.utils.LocalDateTimeAdapter())
            .create();
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructor de la clase
     */
    public void start() throws IOException {
        try {
            openConnection();
            logger.info("🔵 Iniciando conexión:");
            var token = doRequest(LOGIN, null, gson.toJson(new Login("Madi", "madi1234")), "Login realizado", "El login no se ha realizado");
            if (Objects.equals(token, "")) throw new ClientException("No se ha recibido el token");
            logger.info("🔵 Listado de Funkos:");
            doRequest(GETALL, token, null, "Todos los Funkos", "Lista de Funkos no encontrados");
            logger.info("🔵 Buscando Funko por ID:");
            doRequest(GETBYID, token, "ed6c6f58-7c6b-434b-82ab-01d2d6e4434b", "Funko por ID", "Funko por ID no encontrado");
            logger.info("🔵 Listado de Funkos con modelo OTROS:");
            doRequest(GETBYMODEL, token, Model.OTROS.name(), "Funkos por modelo", "Funkos por modelo no encontrados");
            logger.info("🔵 Listado de Funkos con año de lanzamiento de 2023:");
            doRequest(GETBYRELEASEYEAR, token, "2023", "Funkos por año de lanzamiento", "Funkos por año de lanzamiento no encontrados");
            logger.info("🔵 Insertando Funko:");
            Funko funko = Funko.builder().name("ClientFunkoTest").model(Model.OTROS).price(23.12).releaseDate(LocalDate.now()).build();
            doRequest(INSERT, token, gson.toJson(funko), "Funko insertado", "Funko no se ha insertado");
            logger.info("🔵 Modificando Funko:");
            funko.setName("ClientFunkoTestModified");
            doRequest(UPDATE, token, gson.toJson(funko), "Funko actualizado", "Funko no se ha actualizado");
            logger.info("🔵 Eliminando Funko:");
            doRequest(DELETE, token, funko.getCod().toString(), "Funko eliminado", "Funko no se ha eliminado");
            logger.info("🔵 Cerrando cliente:");
            doRequest(EXIT, token, null, "Cliente cerrado", "Cliente no se ha cerrado");
        } catch (ClientException ex) {
            String str = "Error: " + ex.getMessage();
            logger.error(str);
            closeConnection();
            System.exit(1);
        }
    }

    /**
     * Cierra la conexión
     */
    public void closeConnection() {
        logger.info("🔵 Cerrando Cliente");
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            String str = "🔴 Error al cerrar la conexión con el servidor " + host + ":" + port + " -> " + e.getMessage();
            logger.error(str);
        }
    }

    /**
     * Abre la conexión con el servidor
     *
     * @throws IOException Excepción si hay un error en la entrada/salida
     */
    private void openConnection() throws IOException {
        logger.info("🔵 Iniciando Cliente");
        Map<String, String> myConfig = readConfigFile();
        logger.debug("Cargando fichero de propiedades");
        System.setProperty("javax.net.ssl.trustStore", myConfig.get(KEY_FILE_CONFIG_NAME));
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get(KEY_PASSWORD_CONFIG_NAME));
        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) clientFactory.createSocket(ServerConnection.host, ServerConnection.port);
        String str = "Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols());
        logger.debug(str);
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});
        str = "Conectando al servidor: " + ServerConnection.host + ":" + ServerConnection.port;
        logger.debug(str);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        str = "✅ Cliente conectado a " + ServerConnection.host + ":" + ServerConnection.port;
        logger.info(str);
        infoSession(socket);
    }

    /**
     * Realiza una petición al servidor
     *
     * @param type     Tipo de petición
     * @param token    Token de la sesión
     * @param data     Datos de la petición
     * @param validMsg Mensaje de petición válida
     * @param errorMsg Mensaje de petición errónea
     * @return Contenido de la respuesta
     * @throws ClientException Excepción si hay un error en la petición
     * @throws IOException     Excepción si hay un error en la entrada/salida
     */
    private String doRequest(Request.Type type, String token, String data, String validMsg, String errorMsg) throws ClientException, IOException {
        String str = "Petición enviada de tipo: " + type;
        Request request = new Request(type, data, token, LocalDateTime.now().toString());
        logger.info(str);
        str = "Petición enviada: " + request;
        logger.debug(str);
        out.println(gson.toJson(request));
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        str = "Respuesta recibida: " + response.toString();
        logger.debug(str);
        str = "Respuesta recibida de tipo: " + response.status();
        logger.info(str);
        var returnContent = "";
        switch (response.status()) {
            case OK -> {
                var responseContent = gson.fromJson(response.content(), new TypeToken<>() {
                }.getType());
                str = "🟢 " + validMsg + " => " + responseContent;
                logger.info(str);
            }
            case ERROR -> {
                str = "🔴 Error: " + errorMsg + response.content();
                logger.error(str);
            }
            case TOKEN -> {
                str = "🟢 El Token es: " + response.content();
                logger.info(str);
                return response.content();
            }
            case BYE -> {
                str = "Cerrando conexión " + response.content();
                logger.debug(str);
                closeConnection();
            }
            default -> throw new ClientException(errorMsg);
        }
        return returnContent;
    }

    /**
     * Carga el archivo de configuración del cliente y asigna los valores
     *
     * @return Mapa con la configuración
     */
    public static Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de configuración del cliente");
            ApplicationProperties properties = ApplicationProperties.getInstance();

            String keyFile = properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    KEY_FILE_CONFIG_NAME, "./cert/client_keystore.p12");
            String keyPassword = properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    KEY_PASSWORD_CONFIG_NAME, "reth465j5jyjytfg.-");
            ServerConnection.port = Integer.parseInt(properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    "port", "3000"));
            ServerConnection.host = properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    "host", "localhost");

            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Error al procesar el fichero de propiedades o propiedad vacía");
            }
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }
            Map<String, String> configMap = new HashMap<>();
            configMap.put(KEY_FILE_CONFIG_NAME, keyFile);
            configMap.put(KEY_PASSWORD_CONFIG_NAME, keyPassword);
            return configMap;
        } catch (FileNotFoundException e) {
            String str = "Error en la clave: " + e.getLocalizedMessage();
            logger.error(str);
            System.exit(1);
            return Collections.emptyMap();
        }
    }


    /**
     * Imprime información de la sesión en loggers
     *
     * @param socket Socket de la sesión
     */
    private void infoSession(SSLSocket socket) {
        logger.info("Información de la sesión");
        try {
            SSLSession session = socket.getSession();
            String str = "Servidor: " + session.getPeerHost();
            logger.info(str);
            str = "Cifrado: " + session.getCipherSuite();
            logger.info(str);
            str = "Protocolo: " + session.getProtocol();
            logger.info(str);
            str = "Identificador:" + new BigInteger(session.getId());
            logger.info(str);
            str = "Creación de la sesión: " + session.getCreationTime();
            logger.info(str);
            X509Certificate certificado = (X509Certificate) session.getPeerCertificates()[0];
            str = "Propietario del certificado: " + certificado.getSubjectX500Principal();
            logger.info(str);
            str = "Algoritmo del certificado: " + certificado.getSigAlgName();
            logger.info(str);
            str = "Tipo de certificado: " + certificado.getType();
            logger.info(str);
            str = "Número de serie del certificado: " + certificado.getSerialNumber();
            logger.info(str);
            str = "Certificado válido hasta: " + certificado.getNotAfter();
            logger.info(str);
        } catch (SSLPeerUnverifiedException ex) {
            String str = "Error en la sesión: " + ex.getLocalizedMessage();
            logger.error(str);
        }
    }
}