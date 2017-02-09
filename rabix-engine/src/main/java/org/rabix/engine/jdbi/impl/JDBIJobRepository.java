package org.rabix.engine.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.SchemaHelper;
import org.rabix.engine.jdbi.impl.JDBIJobRepository.JobMapper;
import org.rabix.engine.repository.JobRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(JobMapper.class)
public interface JDBIJobRepository extends JobRepository {

  @Override
  @SqlUpdate("insert into job (id,root_id,name, parent_id, status, message, inputs, outputs, resources, group_id,app) values (:id,:root_id,:name,:parent_id,:status::job_status,:message,:inputs::jsonb,:outputs::jsonb,:resources::jsonb,:group_id,:app)")
  void insert(@BindJob Job job, @Bind("group_id") UUID groupId);

  @Override
  @SqlUpdate("update job set root_id=:root_id,name=:name, parent_id=:parent_id, status=:status::job_status, message=:message, inputs=:inputs::jsonb, outputs=:outputs::jsonb, resources=:resources::jsonb,app=:app where id=:id")
  void update(@BindJob Job job);

  @Override
  @SqlQuery("select * from job where id=:id")
  Job get(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from job")
  Set<Job> get();
  
  @Override
  @SqlQuery("select * from job where root_id=:root_id")
  Set<Job> getByRootId(@Bind("root_id") UUID rootId);
  
  @Override
  @SqlQuery("select * from job where group_id=:group_id")
  Set<Job> getJobsByGroupId(@Bind("group_id") UUID group_id);

  public static class JobMapper implements ResultSetMapper<Job> {
    public Job map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      UUID root_id = r.getObject("root_id", UUID.class);
      UUID parent_id = r.getObject("parent_id", UUID.class);
      String name = r.getString("name");
      String app = r.getString("app");
      Job.JobStatus status = Job.JobStatus.valueOf(r.getString("status"));
      String message = r.getString("message");
      String inputsJson = r.getString("inputs");
      String outputsJson = r.getString("outputs");
      String resourcesStr = r.getString("resources");
      Resources res = JSONHelper.readObject(resourcesStr, Resources.class);

      Map<String, Object> inputs = JSONHelper.readMap(inputsJson);
      Map<String, Object> outputs = JSONHelper.readMap(outputsJson);

      return new Job(SchemaHelper.fromUUID(id), SchemaHelper.fromUUID(parent_id), SchemaHelper.fromUUID(root_id), name, app, status, message, inputs, outputs, Collections.emptyMap(), res, Collections.emptySet());
    }
  }

  @BindingAnnotation(JDBIJobRepository.BindJob.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJob {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIJobRepository.BindJob, Job> build(Annotation annotation) {
        return new Binder<JDBIJobRepository.BindJob, Job>() {
          public void bind(SQLStatement<?> q, JDBIJobRepository.BindJob bind, Job job) {
            q.bind("id", SchemaHelper.toUUID(job.getId()));
            q.bind("root_id", SchemaHelper.toUUID(job.getRootId()));
            q.bind("name", job.getName());
            q.bind("parent_id", SchemaHelper.toUUID(job.getParentId()));
            q.bind("status", job.getStatus().toString());
            q.bind("message", job.getMessage());
            q.bind("inputs", JSONHelper.writeObject(job.getInputs()));
            q.bind("outputs", JSONHelper.writeObject(job.getOutputs()));
            q.bind("app", job.getApp());
            q.bind("resources", JSONHelper.writeObject(job.getResources()));
          }
        };
      }
    }
  }

}