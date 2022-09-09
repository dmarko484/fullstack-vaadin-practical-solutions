package app.todo.repo;

import app.todo.model.Todo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InmemoryRepository {

  private final List<Todo> items = new ArrayList<>();



  public void addToItems(Todo todo) {
    items.add(todo);
  }

  public List<Todo> getAllItems() {
    return items;
  }

}
