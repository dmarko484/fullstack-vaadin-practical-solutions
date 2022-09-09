package app.todo.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URLConnection;

@Service
public class AttachmentsService {

  @Autowired
  GridFsOperations gridFsOperations;

  @Autowired
  @Getter
  GridFsTemplate gridFsTemplate;

  public GridFSFile storeInGridFS(InputStream inputStream, String fileName, long size) {

    DBObject metadata = new BasicDBObject();
    metadata.put("name", fileName);
    metadata.put("size", size);
    metadata.put("mime", URLConnection.guessContentTypeFromName(fileName));

    ObjectId objectId = gridFsOperations.store(inputStream, fileName, metadata);
    return gridFsOperations.findOne(new Query(Criteria.where("_id").is(objectId)));
  }

  public GridFSFindIterable listAllFiles() {
    return gridFsOperations.find(new Query());
  }

  public void deleteAttachment(String id) {
    gridFsOperations.delete(new Query(GridFsCriteria.where("_id").is(id)));
  }

}
