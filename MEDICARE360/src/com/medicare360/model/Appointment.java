package com.medicare360.model;

/** One appointment between a patient and a doctor. */
public class Appointment {

    public static final String SCHEDULED = "Scheduled";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";

    private int id;
    private int patientId;
    private int doctorId;
    private String patientName;   // filled in by database JOINs
    private String doctorName;    // filled in by database JOINs
    private double doctorFee;     // doctor's consultation fee (used by billing)
    private String date;          // yyyy-MM-dd
    private String time;          // HH:mm
    private String reason;
    private String status;

    public Appointment(int id, int patientId, int doctorId, String patientName, String doctorName,
                       double doctorFee, String date, String time, String reason, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.doctorFee = doctorFee;
        this.date = date;
        this.time = time;
        this.reason = reason;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public double getDoctorFee() { return doctorFee; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }

    /** Text shown inside the billing drop-down list. */
    @Override
    public String toString() {
        return "#" + id + "  " + date + " " + time + "  with " + doctorName;
    }
}
