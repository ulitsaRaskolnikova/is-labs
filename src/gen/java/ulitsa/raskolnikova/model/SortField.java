package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import ulitsa.raskolnikova.model.SortDirection;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class SortField   {
  
  private String field;

  private SortDirection direction;

  /**
   **/
  public SortField field(String field) {
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
  public SortField direction(SortDirection direction) {
    this.direction = direction;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("direction")
  public SortDirection getDirection() {
    return direction;
  }
  public void setDirection(SortDirection direction) {
    this.direction = direction;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SortField sortField = (SortField) o;
    return Objects.equals(this.field, sortField.field) &&
        Objects.equals(this.direction, sortField.direction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, direction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SortField {\n");
    
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
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

