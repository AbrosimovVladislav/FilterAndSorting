package filterandsorting;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface FilterAndSortingRepository<T> {
	List<T> findAllByParameters(Map<String, String> requestParams, Pageable pageable, Class entityClass);
}
