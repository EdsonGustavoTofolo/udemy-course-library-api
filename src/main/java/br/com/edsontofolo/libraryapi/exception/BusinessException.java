package br.com.edsontofolo.libraryapi.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String error) {
        super(error);
    }
}
