package com.fsbergado.mppeis.exceptions;

/**
 * RecordNotFoundException
 */
public class RecordNotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String message = "Record not found.";

    public RecordNotFoundException() {
    }

    public String getMessage() {
        return message;
    }

}