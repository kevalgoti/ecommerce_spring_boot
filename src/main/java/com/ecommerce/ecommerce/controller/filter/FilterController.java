package com.ecommerce.ecommerce.controller.filter;

import com.ecommerce.ecommerce.dto.filter.FilterRequestDTO;
import com.ecommerce.ecommerce.service.DynamicFilterService.DynamicFilterService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/filter")
public class FilterController {
    private final DynamicFilterService filterService;

    public FilterController(DynamicFilterService filterService) {
        this.filterService = filterService;
    }

    @PostMapping(value = "/data")
    public List<?> filter(@RequestBody FilterRequestDTO filterRequest) {
        return filterService.filterData(filterRequest);
    }
}
