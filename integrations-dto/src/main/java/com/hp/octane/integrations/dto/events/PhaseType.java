package com.hp.octane.integrations.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by gadiel on 10/10/2016.
 */



public enum PhaseType {
    POST("post"),
    INTERNAL("internal");


    private String value;

    PhaseType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static PhaseType fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("value MUST NOT be null nor empty");
        }

        PhaseType result = POST;
        for (PhaseType v : values()) {
            if (v.value.compareTo(value) == 0) {
                result = v;
                break;
            }
        }
        return result;
    }
}