package app.todo.service;

import app.todo.model.Todo;
import app.todo.repo.TodoMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@Profile("mongo")
public class MongoDBService implements DbService {

  @Autowired
  TodoMongoDBRepository repo;

  @PostConstruct
  void setup() {
    log.info("+++ MongoDB database is starting ...");
    repo.deleteAll();

    for (int i = 1; i <= 8; i++) {
      addToItems(Todo.builder().title("Todo number " + i).body("Body number " + i).author("admin").createdAt(LocalDateTime.now()).build());
    }
  }

  @Override
  public List<Todo> getAllItems() {
    return StreamSupport.stream(repo.findAll().spliterator(), false).collect(Collectors.toList());
  }

  @Override
  public void addToItems(Todo todo) {
    repo.save(todo);
  }

  @Override
  public void removeItems(Set<Todo> items) {
    repo.deleteAll(items);
  }
}
