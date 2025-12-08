package ulitsa.raskolnikova.model;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Country {

    @JsonProperty("RUSSIA") RUSSIA(String.valueOf("RUSSIA")), @JsonProperty("UNITED_KINGDOM") UNITED_KINGDOM(String.valueOf("UNITED_KINGDOM")), @JsonProperty("SPAIN") SPAIN(String.valueOf("SPAIN")), @JsonProperty("ITALY") ITALY(String.valueOf("ITALY")), @JsonProperty("SOUTH_KOREA") SOUTH_KOREA(String.valueOf("SOUTH_KOREA"));


    private String value;

    Country(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static Country fromValue(String value) {
        for (Country b : Country.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}



