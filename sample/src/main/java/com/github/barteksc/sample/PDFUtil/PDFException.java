package com.github.barteksc.sample.PDFUtil;

public class PDFException extends Exception {

    private String message;

    public PDFException() {
        super();
    }

    public PDFException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
}
