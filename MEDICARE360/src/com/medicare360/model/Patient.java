package com.medicare360.model;

/** A hospital patient. Extends {@link Person} with medical/registration details. */
public class Patient extends Person {

    private int age;
    private String gender;
    private String bloodGroup;
    private String address;
    private String notes;
    private String registeredDate;

    public Patient(int id, String name, String phone, int age, String gender,
                   String bloodGroup, String address, String notes, String registeredDate) {
        super(id, name, phone);
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.notes = notes;
        this.registeredDate = registeredDate;
    }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }

    @Override
    public String getRole() { return "Patient"; }

    /** Text shown inside drop-down lists, e.g. "#3  Rahul Verma". */
    @Override
    public String toString() {
        return "#" + getId() + "  " + getName();
    }
}
