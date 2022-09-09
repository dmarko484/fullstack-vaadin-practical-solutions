package app.todo.service;

import app.todo.model.Todo;
import app.todo.repo.InmemoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Profile("memory")
public class InMemoryDbService implements DbService {

  @Autowired
  InmemoryRepository inmemoryRepository;

  @PostConstruct
  void setup() {
    log.info("+++ InMemory database is starting ...");

    for (int i = 1; i <= 6; i++) {
      addToItems(Todo.builder().title("Todo number " + i).body("Body number " + i).author("admin").createdAt(LocalDateTime.now()).build());
    }
  }

  @Override
  public List<Todo> getAllItems() {
    return inmemoryRepository.getAllItems();
  }

  @Override
  public void addToItems(Todo todo) {
    inmemoryRepository.addToItems(todo);
  }

  @Override
  public void removeItems(Set<Todo> items) {
    inmemoryRepository.getAllItems().removeAll(items);
  }
}
