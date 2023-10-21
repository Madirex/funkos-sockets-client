package com.madirex;

/**
 * Clase ClientApp
 */
public class ClientApp {
    private static ClientApp clientAppInstance;

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

    }
}
