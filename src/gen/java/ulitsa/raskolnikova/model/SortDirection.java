package ulitsa.raskolnikova.model;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum SortDirection {

    @JsonProperty("ASC") ASC(String.valueOf("ASC")), @JsonProperty("DESC") DESC(String.valueOf("DESC"));


    private String value;

    SortDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static SortDirection fromValue(String value) {
        for (SortDirection b : SortDirection.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}



