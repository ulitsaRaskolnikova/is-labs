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



public class PersonResponse   {
  
  private Integer id;

  private String name;

  private Integer coordinatesId;

  private Color eyeColor;

  private Color hairColor;

  private Integer locationId;

  private Integer height;

  private Country nationality;

  private java.util.Date creationDate;

  /**
   * minimum: 1
   **/
  public PersonResponse id(Integer id) {
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
  public PersonResponse name(String name) {
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
  public PersonResponse coordinatesId(Integer coordinatesId) {
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
  public PersonResponse eyeColor(Color eyeColor) {
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
  public PersonResponse hairColor(Color hairColor) {
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
  public PersonResponse locationId(Integer locationId) {
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
  public PersonResponse height(Integer height) {
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
  public PersonResponse nationality(Country nationality) {
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


  /**
   **/
  public PersonResponse creationDate(java.util.Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("creationDate")
  @NotNull
  public java.util.Date getCreationDate() {
    return creationDate;
  }
  public void setCreationDate(java.util.Date creationDate) {
    this.creationDate = creationDate;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PersonResponse personResponse = (PersonResponse) o;
    return Objects.equals(this.id, personResponse.id) &&
        Objects.equals(this.name, personResponse.name) &&
        Objects.equals(this.coordinatesId, personResponse.coordinatesId) &&
        Objects.equals(this.eyeColor, personResponse.eyeColor) &&
        Objects.equals(this.hairColor, personResponse.hairColor) &&
        Objects.equals(this.locationId, personResponse.locationId) &&
        Objects.equals(this.height, personResponse.height) &&
        Objects.equals(this.nationality, personResponse.nationality) &&
        Objects.equals(this.creationDate, personResponse.creationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, coordinatesId, eyeColor, hairColor, locationId, height, nationality, creationDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PersonResponse {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    coordinatesId: ").append(toIndentedString(coordinatesId)).append("\n");
    sb.append("    eyeColor: ").append(toIndentedString(eyeColor)).append("\n");
    sb.append("    hairColor: ").append(toIndentedString(hairColor)).append("\n");
    sb.append("    locationId: ").append(toIndentedString(locationId)).append("\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    nationality: ").append(toIndentedString(nationality)).append("\n");
    sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
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

