package com.madirex.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase ServerConnection
 */
public class ServerConnection {

    public static final String KEY_FILE_CONFIG_NAME = "keyFile";
    public static final String KEY_PASSWORD_CONFIG_NAME = "keyPassword";
    private static ServerConnection serverConnectionInstance;
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private final Logger logger = LoggerFactory.getLogger(ServerConnection.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.madirex.utils.LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new com.madirex.utils.LocalDateTimeAdapter()).create();
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Constructor de la clase
     */
    private ServerConnection() {
    }

    /**
     * Singleton de la clase
     *
     * @return Instancia de la clase
     */
    public static ServerConnection getInstance() {
        if (serverConnectionInstance == null) {
            serverConnectionInstance = new ServerConnection();
        }
        return serverConnectionInstance;
    }

    /**
     * Cierra la conexión
     */
    private void closeConnection() {
        logger.info("🔵 Cerrando Cliente");
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            String str = "🔴 Error al cerrar la conexión con el servidor " + HOST + ":" + PORT + " -> " + e.getMessage();
            logger.error(str);
        }
    }

    /**
     * Abre la conexión con el servidor
     */
    private void openConnection() {
        logger.info("🔵 Iniciando Cliente");
        Map<String, String> myConfig = readConfigFile();
        logger.debug("Cargando fichero de propiedades");
        System.setProperty("javax.net.ssl.trustStore", myConfig.get(KEY_FILE_CONFIG_NAME));
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get(KEY_PASSWORD_CONFIG_NAME));
        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket sslsocket = (SSLSocket) clientFactory.createSocket(HOST, PORT)) {
            String str = "Protocolos soportados: " + Arrays.toString(sslsocket.getSupportedProtocols());
            logger.debug(str);
            sslsocket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
            sslsocket.setEnabledProtocols(new String[]{"TLSv1.3"});
            str = "Conectando al servidor: " + HOST + ":" + PORT;
            logger.debug(str);
            out = new PrintWriter(sslsocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            str = "✅ Cliente conectado a " + HOST + ":" + PORT;
            logger.info(str);
            infoSession(sslsocket);
        } catch (IOException e) {
            String str = "🔴 Error al abrir la conexión con el servidor " + HOST + ":" + PORT + " -> " + e.getMessage();
            logger.error(str);
        }
    }

    /**
     * Carga el archivo de configuración del cliente
     *
     * @return Mapa con la configuración
     */
    public Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de configuración del cliente");
            ApplicationProperties properties = ApplicationProperties.getInstance();

            String keyFile = properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    KEY_FILE_CONFIG_NAME, "./cert/client_keystore.p12");
            String keyPassword = properties.readProperty(ApplicationProperties.PropertyType.CLIENT,
                    KEY_PASSWORD_CONFIG_NAME, "reth465j5jyjytfg.-");

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
