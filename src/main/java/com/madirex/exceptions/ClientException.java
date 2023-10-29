package com.madirex.exceptions;

/**
 * Clase ClientException
 */
public class ClientException extends Exception {
    /**
     * Constructor de la clase
     *
     * @param message Mensaje de la excepción
     */
    public ClientException(String message) {
        super(message);
    }
}