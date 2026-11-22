package com.medicare360.db;

import java.sql.SQLException;
import java.util.List;

/**
 * A simple INTERFACE that every data-access class (DAO) in this project implements.
 * "T" is a GENERIC type: Dao<Patient> works with patients, Dao<Doctor> with doctors, and so on.
 */
public interface Dao<T> {

    /** Returns every record of this type. */
    List<T> findAll() throws SQLException;

    /** Saves a new record permanently. The generated id is written back into the object. */
    void insert(T item) throws SQLException;
}
