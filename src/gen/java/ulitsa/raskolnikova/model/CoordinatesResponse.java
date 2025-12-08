package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class CoordinatesResponse   {
  
  private Integer id;

  private Double x;

  private Float y;

  /**
   * minimum: 1
   **/
  public CoordinatesResponse id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
 @Min(1)  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }


  /**
   **/
  public CoordinatesResponse x(Double x) {
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
  public CoordinatesResponse y(Float y) {
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
    CoordinatesResponse coordinatesResponse = (CoordinatesResponse) o;
    return Objects.equals(this.id, coordinatesResponse.id) &&
        Objects.equals(this.x, coordinatesResponse.x) &&
        Objects.equals(this.y, coordinatesResponse.y);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, x, y);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CoordinatesResponse {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

