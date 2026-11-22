package com.medicare360.model;

/** A doctor working at the hospital. Extends {@link Person}. */
public class Doctor extends Person {

    private String specialization;
    private String email;
    private double consultationFee;

    public Doctor(int id, String name, String phone, String specialization,
                  String email, double consultationFee) {
        super(id, name, phone);
        this.specialization = specialization;
        this.email = email;
        this.consultationFee = consultationFee;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    @Override
    public String getRole() { return "Doctor"; }

    /** Text shown inside drop-down lists, e.g. "Dr. Rohan Mehta (General Medicine)". */
    @Override
    public String toString() {
        return getName() + " (" + specialization + ")";
    }
}
