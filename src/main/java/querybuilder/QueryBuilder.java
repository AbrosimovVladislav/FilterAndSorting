package querybuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QueryBuilder {

    private final QBParamExtractor qbParamExtractor;

    public <T> CriteriaQuery<T> createCriteriaQueryFromParamMap(CriteriaBuilder criteriaBuilder,
                                                                  Map<String, String> requestParams,
                                                                  Class<T> entityClass) {
        List<QBParam> dslParams = parseRequestParamMap(requestParams);

        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);

        Predicate[] predicates = dslParams.stream()
                .map(qbParam -> createSinglePredicate(criteriaBuilder, root, qbParam))
                .toArray(Predicate[]::new);
        criteriaQuery.where(predicates);
        return criteriaQuery;
    }

    public <T> CriteriaQuery<T> addSort(CriteriaBuilder cb,
                                          CriteriaQuery<T> cq,
                                          Pageable pageable) {
        Order sortingPredicate = createSortingPredicate(cb, cq.getRoots().iterator().next(), pageable);
        return cq.orderBy(sortingPredicate);
    }

    private Predicate createSinglePredicate(CriteriaBuilder criteriaBuilder, Root root, QBParam qbParam) {
        List<String> entities = qbParam.entities;
        Path path = buildPath(qbParam.paramName, entities, root);
        return qbParam.operation.getPredicate(qbParam.paramValue, criteriaBuilder, path);
    }

    private Path buildPath(String paramName, List<String> entities, Root root) {
        Path path;
        if (entities.isEmpty()) {
            path = root.get(paramName);
        } else {
            path = root.join(entities.get(0));
            for (int i = 1; i < entities.size(); i++) {
                path = ((Join) path).join(entities.get(i));
            }
            path = path.get(paramName);
        }
        return path;
    }

    private Order createSortingPredicate(CriteriaBuilder criteriaBuilder, Root root, Pageable pageable) {
        Sort.Order sortingOrder = pageable.getSort().iterator().next();
        String sortingPropertyKey = sortingOrder.getProperty();
        List<String> entitiesAndPropertyName = new ArrayList<String>() {{
            this.addAll(Arrays.asList(sortingPropertyKey.split("\\.")));
        }};
        String propertyName = entitiesAndPropertyName.remove(entitiesAndPropertyName.size() - 1);
        Path sortingPath = buildPath(propertyName, entitiesAndPropertyName, root);
        return sortingOrder.isAscending()
                ? criteriaBuilder.asc(sortingPath)
                : criteriaBuilder.desc(sortingPath);
    }

    private List<QBParam> parseRequestParamMap(Map<String, String> requestParams) {
        return requestParams.entrySet()
                .stream()
                .map(e -> qbParamExtractor.extractQbParam(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

}
