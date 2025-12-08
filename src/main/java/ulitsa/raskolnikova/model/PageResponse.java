package ulitsa.raskolnikova.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class PageResponse<T> {
    private List<T> content = new ArrayList<>();

    private Integer totalElements;

    private Integer totalPages;
}
