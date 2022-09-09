package app.todo.repo;

import app.todo.model.Todo;
import org.springframework.data.repository.CrudRepository;

public interface TodoMongoDBRepository extends CrudRepository<Todo, String> {
}
