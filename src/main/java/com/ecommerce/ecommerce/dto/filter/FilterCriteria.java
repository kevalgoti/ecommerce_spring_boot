package com.ecommerce.ecommerce.dto.filter;

import lombok.Data;

import java.util.List;

@Data
public class FilterCriteria {
    private String field;  // Field to filter
    private String type;   // IN, GTE, LTE, OR
    private List<Object> values; // List of values (for IN, GTE, LTE)
    private List<FilterCriteria> filters; // Nested filters for OR condition
}
