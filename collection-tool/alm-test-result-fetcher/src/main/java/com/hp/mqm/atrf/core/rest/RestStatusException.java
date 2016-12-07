package com.hp.mqm.atrf.core.rest;

/**
 * Thrown if status is not 200 or 201
 * Created by berkovir on 20/11/2016.
 */
public class RestStatusException extends RuntimeException {
    private Response response;

    public RestStatusException(Response response ){
        super(response.getResponseData());
        this.response = response;
    }
    public Response getResponse() {
        return response;
    }
}
