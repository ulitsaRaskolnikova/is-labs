package ulitsa.raskolnikova.model;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Color {

    @JsonProperty("RED") RED(String.valueOf("RED")), @JsonProperty("BLACK") BLACK(String.valueOf("BLACK")), @JsonProperty("BLUE") BLUE(String.valueOf("BLUE")), @JsonProperty("WHITE") WHITE(String.valueOf("WHITE")), @JsonProperty("BROWN") BROWN(String.valueOf("BROWN"));


    private String value;

    Color(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static Color fromValue(String value) {
        for (Color b : Color.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}



