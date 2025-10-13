package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class LocationResponse   {
  
  private Integer id;

  private Double x;

  private Double y;

  private Integer z;

  /**
   * minimum: 1
   **/
  public LocationResponse id(Integer id) {
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
  public LocationResponse x(Double x) {
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
   **/
  public LocationResponse y(Double y) {
    this.y = y;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("y")
  @NotNull
  public Double getY() {
    return y;
  }
  public void setY(Double y) {
    this.y = y;
  }


  /**
   **/
  public LocationResponse z(Integer z) {
    this.z = z;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("z")
  @NotNull
  public Integer getZ() {
    return z;
  }
  public void setZ(Integer z) {
    this.z = z;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationResponse locationResponse = (LocationResponse) o;
    return Objects.equals(this.id, locationResponse.id) &&
        Objects.equals(this.x, locationResponse.x) &&
        Objects.equals(this.y, locationResponse.y) &&
        Objects.equals(this.z, locationResponse.z);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, x, y, z);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LocationResponse {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    x: ").append(toIndentedString(x)).append("\n");
    sb.append("    y: ").append(toIndentedString(y)).append("\n");
    sb.append("    z: ").append(toIndentedString(z)).append("\n");
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

