package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class CoordinatesRequest   {
  
  private Double x;

  private Float y;

  /**
   **/
  public CoordinatesRequest x(Double x) {
    this.x = x;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("x")
  @NotNull
  public Double getX() {
    return x;
  }
  public void setX(Double x) {
    this.x = x;
  }


  /**
   * maximum: 258
   **/
  public CoordinatesRequest y(Float y) {
    this.y = y;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("y")
  @NotNull
 @DecimalMax("258")  public Float getY() {
    return y;
  }
  public void setY(Float y) {
    this.y = y;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoordinatesRequest coordinatesRequest = (CoordinatesRequest) o;
    return Objects.equals(this.x, coordinatesRequest.x) &&
        Objects.equals(this.y, coordinatesRequest.y);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CoordinatesRequest {\n");
    
    sb.append("    x: ").append(toIndentedString(x)).append("\n");
    sb.append("    y: ").append(toIndentedString(y)).append("\n");
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

