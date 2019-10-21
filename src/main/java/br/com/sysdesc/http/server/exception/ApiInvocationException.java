package br.com.sysdesc.http.server.exception;

public class ApiInvocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApiInvocationException(String error) {
        super(error);
    }
}
