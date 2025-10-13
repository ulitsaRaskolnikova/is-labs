package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.openapitools.jackson.nullable.JsonNullable;
import ulitsa.raskolnikova.model.Color;
import ulitsa.raskolnikova.model.Country;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class PersonRequest   {
  
  private String name;

  private Integer coordinatesId;

  private Color eyeColor;

  private Color hairColor;

  private Integer locationId;

  private Integer height;

  private Country nationality;

  /**
   **/
  public PersonRequest name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
 @Size(min=1)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }


  /**
   * minimum: 1
   **/
  public PersonRequest coordinatesId(Integer coordinatesId) {
    this.coordinatesId = coordinatesId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("coordinatesId")
  @NotNull
 @Min(1)  public Integer getCoordinatesId() {
    return coordinatesId;
  }
  public void setCoordinatesId(Integer coordinatesId) {
    this.coordinatesId = coordinatesId;
  }


  /**
   **/
  public PersonRequest eyeColor(Color eyeColor) {
    this.eyeColor = eyeColor;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("eyeColor")
  @NotNull
  public Color getEyeColor() {
    return eyeColor;
  }
  public void setEyeColor(Color eyeColor) {
    this.eyeColor = eyeColor;
  }


  /**
   **/
  public PersonRequest hairColor(Color hairColor) {
    this.hairColor = hairColor;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("hairColor")
  @NotNull
  public Color getHairColor() {
    return hairColor;
  }
  public void setHairColor(Color hairColor) {
    this.hairColor = hairColor;
  }


  /**
   * minimum: 1
   **/
  public PersonRequest locationId(Integer locationId) {
    this.locationId = locationId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("locationId")
 @Min(1)  public Integer getLocationId() {
    return locationId;
  }
  public void setLocationId(Integer locationId) {
    this.locationId = locationId;
  }


  /**
   * minimum: 1
   **/
  public PersonRequest height(Integer height) {
    this.height = height;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("height")
 @Min(1)  public Integer getHeight() {
    return height;
  }
  public void setHeight(Integer height) {
    this.height = height;
  }


  /**
   **/
  public PersonRequest nationality(Country nationality) {
    this.nationality = nationality;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("nationality")
  public Country getNationality() {
    return nationality;
  }
  public void setNationality(Country nationality) {
    this.nationality = nationality;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonRequest personRequest = (PersonRequest) o;
    return Objects.equals(this.name, personRequest.name) &&
        Objects.equals(this.coordinatesId, personRequest.coordinatesId) &&
        Objects.equals(this.eyeColor, personRequest.eyeColor) &&
        Objects.equals(this.hairColor, personRequest.hairColor) &&
        Objects.equals(this.locationId, personRequest.locationId) &&
        Objects.equals(this.height, personRequest.height) &&
        Objects.equals(this.nationality, personRequest.nationality);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, coordinatesId, eyeColor, hairColor, locationId, height, nationality);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PersonRequest {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    coordinatesId: ").append(toIndentedString(coordinatesId)).append("\n");
    sb.append("    eyeColor: ").append(toIndentedString(eyeColor)).append("\n");
    sb.append("    hairColor: ").append(toIndentedString(hairColor)).append("\n");
    sb.append("    locationId: ").append(toIndentedString(locationId)).append("\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    nationality: ").append(toIndentedString(nationality)).append("\n");
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

