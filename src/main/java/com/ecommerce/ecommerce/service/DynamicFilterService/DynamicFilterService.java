package com.ecommerce.ecommerce.service.DynamicFilterService;

import com.ecommerce.ecommerce.dto.filter.FilterCriteria;
import com.ecommerce.ecommerce.dto.filter.FilterRequestDTO;
import com.ecommerce.ecommerce.model.order.Order;
import com.ecommerce.ecommerce.model.product.Product;
import com.ecommerce.ecommerce.model.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DynamicFilterService {
    private final EntityManager entityManager;
    private static final List<Class<?>> ENTITY_CLASSES = List.of(Order.class, Product.class, User.class);

    public DynamicFilterService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<?> filterData(FilterRequestDTO request) {
        FilterCriteria filters = request.getFilters();

        // Dynamically determine entity based on the field name
        Class<?> entityClass = findEntityByField(filters.getField());
        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown entity for field: " + filters.getField());
        }

        // Build a dynamic query using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = cb.createQuery(entityClass);
        Root<?> root = query.from(entityClass);

        Predicate predicate = buildPredicate(cb, root, filters);
        query.where(predicate);

        TypedQuery<?> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<?> root, FilterCriteria filter) {
        Path<?> fieldPath = root.get(filter.getField());

        // Check if the field is an Enum
        if (fieldPath.getJavaType().isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) fieldPath.getJavaType();
            List<Enum> enumValues = filter.getValues().stream()
                    .map(value -> Enum.valueOf(enumClass, value.toString()))
                    .toList();
            return fieldPath.in(enumValues);
        }

        // Handle different filter types
        switch (filter.getType().toUpperCase()) {
            case "IN":
                return root.get(filter.getField()).in(filter.getValues());
            case "GTE":
                return cb.greaterThanOrEqualTo(root.get(filter.getField()), filter.getValues().get(0).toString());
            case "LTE":
                return cb.lessThanOrEqualTo(root.get(filter.getField()), filter.getValues().get(0).toString());
            case "OR":
                if (filter.getFilters() == null || filter.getFilters().size() != 2) {
                    throw new IllegalArgumentException("Invalid OR condition: Two sub-filters are required.");
                }
                Predicate predicate1 = buildPredicate(cb, root, filter.getFilters().get(0));
                Predicate predicate2 = buildPredicate(cb, root, filter.getFilters().get(1));
                return cb.or(predicate1, predicate2);
            default:
                throw new IllegalArgumentException("Unsupported filter type: " + filter.getType());
        }
    }

    private Comparable<?> convertToComparable(Object value, Class<?> fieldType) {
        if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.parseInt(value.toString());
        } else if (fieldType == Double.class || fieldType == double.class) {
            return Double.parseDouble(value.toString());
        } else if (fieldType == Float.class || fieldType == float.class) {
            return Float.parseFloat(value.toString());
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.parseLong(value.toString());
        } else if (fieldType == String.class) {
            return value.toString();
        }
        throw new IllegalArgumentException("Unsupported data type: " + fieldType.getName());
    }

    private Class<?> findEntityByField(String field) {
        return ENTITY_CLASSES.stream()
                .filter(entity -> Arrays.stream(entity.getDeclaredFields())
                        .anyMatch(f -> f.getName().equalsIgnoreCase(field)))
                .findFirst()
                .orElse(null);
    }
}
