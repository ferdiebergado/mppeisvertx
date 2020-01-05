package com.fsbergado.mppeis.database;

import java.util.List;

/**
 * DAO
 */
public interface DAO {

    public Object findById(Long id);

    public List<Object> getAll();

    public List<Object> findBy(String field);

}