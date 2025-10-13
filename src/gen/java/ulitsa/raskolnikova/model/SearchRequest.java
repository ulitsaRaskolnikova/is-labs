package ulitsa.raskolnikova.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ulitsa.raskolnikova.model.FilterField;
import ulitsa.raskolnikova.model.SortField;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;


import io.swagger.annotations.*;
import java.util.Objects;



public class SearchRequest   {
  
  private Integer page = 0;

  private Integer size = 20;

  private List<@Valid SortField> sort = new ArrayList<>();

  private List<@Valid FilterField> filters = new ArrayList<>();

  /**
   * minimum: 0
   **/
  public SearchRequest page(Integer page) {
    this.page = page;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("page")
 @Min(0)  public Integer getPage() {
    return page;
  }
  public void setPage(Integer page) {
    this.page = page;
  }


  /**
   * minimum: 1
   **/
  public SearchRequest size(Integer size) {
    this.size = size;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("size")
 @Min(1)  public Integer getSize() {
    return size;
  }
  public void setSize(Integer size) {
    this.size = size;
  }


  /**
   **/
  public SearchRequest sort(List<@Valid SortField> sort) {
    this.sort = sort;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("sort")
  public List<@Valid SortField> getSort() {
    return sort;
  }
  public void setSort(List<@Valid SortField> sort) {
    this.sort = sort;
  }

  public SearchRequest addSortItem(SortField sortItem) {
    if (this.sort == null) {
      this.sort = new ArrayList<>();
    }
    this.sort.add(sortItem);
    return this;
  }


  /**
   **/
  public SearchRequest filters(List<@Valid FilterField> filters) {
    this.filters = filters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("filters")
  public List<@Valid FilterField> getFilters() {
    return filters;
  }
  public void setFilters(List<@Valid FilterField> filters) {
    this.filters = filters;
  }

  public SearchRequest addFiltersItem(FilterField filtersItem) {
    if (this.filters == null) {
      this.filters = new ArrayList<>();
    }
    this.filters.add(filtersItem);
    return this;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchRequest searchRequest = (SearchRequest) o;
    return Objects.equals(this.page, searchRequest.page) &&
        Objects.equals(this.size, searchRequest.size) &&
        Objects.equals(this.sort, searchRequest.sort) &&
        Objects.equals(this.filters, searchRequest.filters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, size, sort, filters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchRequest {\n");
    
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
    sb.append("    filters: ").append(toIndentedString(filters)).append("\n");
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

