package com.ecommerce.ecommerce.dto.filter;

import lombok.Data;

@Data
public class FilterRequestDTO {
    private String table;
    private FilterCriteria filters;
}
