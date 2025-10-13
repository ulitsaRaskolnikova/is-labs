package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ulitsa.raskolnikova.model.PersonResponse;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class PagePerson   {
  
  private List<PersonResponse> content = new ArrayList<>();

  private Integer totalElements;

  private Integer totalPages;

  /**
   **/
  public PagePerson content(List<PersonResponse> content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("content")
  public List<PersonResponse> getContent() {
    return content;
  }
  public void setContent(List<PersonResponse> content) {
    this.content = content;
  }

  public PagePerson addContentItem(PersonResponse contentItem) {
    if (this.content == null) {
      this.content = new ArrayList<>();
    }
    this.content.add(contentItem);
    return this;
  }


  /**
   **/
  public PagePerson totalElements(Integer totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("totalElements")
  public Integer getTotalElements() {
    return totalElements;
  }
  public void setTotalElements(Integer totalElements) {
    this.totalElements = totalElements;
  }


  /**
   **/
  public PagePerson totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("totalPages")
  public Integer getTotalPages() {
    return totalPages;
  }
  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PagePerson pagePerson = (PagePerson) o;
    return Objects.equals(this.content, pagePerson.content) &&
        Objects.equals(this.totalElements, pagePerson.totalElements) &&
        Objects.equals(this.totalPages, pagePerson.totalPages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, totalElements, totalPages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PagePerson {\n");
    
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
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

