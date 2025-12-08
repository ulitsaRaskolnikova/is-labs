package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class FilterField   {
  
  private String field;

  private String pattern;

  /**
   **/
  public FilterField field(String field) {
    this.field = field;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("field")
  public String getField() {
    return field;
  }
  public void setField(String field) {
    this.field = field;
  }


  /**
   **/
  public FilterField pattern(String pattern) {
    this.pattern = pattern;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("pattern")
  public String getPattern() {
    return pattern;
  }
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FilterField filterField = (FilterField) o;
    return Objects.equals(this.field, filterField.field) &&
        Objects.equals(this.pattern, filterField.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, pattern);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FilterField {\n");
    
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    pattern: ").append(toIndentedString(pattern)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

