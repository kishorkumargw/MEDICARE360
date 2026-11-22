package com.medicare360.model;

/**
 * Base class for every person stored in the system (patients and doctors).
 * Demonstrates INHERITANCE and ENCAPSULATION: fields are private and are
 * only reachable through getters and setters.
 */
public abstract class Person {

    private int id;
    private String name;
    private String phone;

    protected Person(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Each subclass says what kind of person it is (POLYMORPHISM). */
    public abstract String getRole();

    @Override
    public String toString() {
        return name;
    }
}
