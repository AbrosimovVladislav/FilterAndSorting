package filterandsorting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import querybuilder.QueryBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilterAndSortingRepositoryImpl<T> implements FilterAndSortingRepository<T> {

    @PersistenceContext
    private final EntityManager entityManager;
    private final QueryBuilder queryBuilder;

    @Override
    public List<T> findAllByParameters(Map<String, String> requestParams, Pageable pageable, Class entityClass) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery =
                queryBuilder.createCriteriaQueryFromParamMap(criteriaBuilder, requestParams, entityClass);
        queryBuilder.addSort(criteriaBuilder, criteriaQuery, pageable);
        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery = addPagination(typedQuery, pageable);
        return typedQuery.getResultList();
    }

    private TypedQuery<T> addPagination(TypedQuery<T> typedQuery, Pageable pageable) {
        typedQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        typedQuery.setMaxResults(pageable.getPageSize());
        return typedQuery;
    }

}
