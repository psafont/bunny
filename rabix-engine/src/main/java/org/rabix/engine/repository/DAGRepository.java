package org.rabix.engine.repository;

import org.rabix.bindings.model.dag.DAGNode;

public interface DAGRepository {

  void insert(String id, DAGNode dag);
  
  DAGNode get(String id, String rootId);
}