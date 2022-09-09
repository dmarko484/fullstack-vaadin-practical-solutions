package app.todo.service;

import app.todo.model.Todo;

import java.util.List;
import java.util.Set;

public interface DbService {
  List<Todo> getAllItems();

  void addToItems(Todo todo);

  void removeItems(Set<Todo> items);

}
