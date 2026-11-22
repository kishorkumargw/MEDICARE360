package com.medicare360.model;

/** A bill generated for a patient. */
public class Bill {

    public static final String PAID = "Paid";
    public static final String PENDING = "Pending";

    private int id;
    private int patientId;
    private Integer appointmentId;   // null when the bill is not linked to an appointment
    private String patientName;      // filled in by database JOIN
    private double consultationFee;
    private double medicineCharges;
    private double otherCharges;
    private double total;
    private String status;
    private String billDate;

    public Bill(int id, int patientId, Integer appointmentId, String patientName,
                double consultationFee, double medicineCharges, double otherCharges,
                String status, String billDate) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.consultationFee = consultationFee;
        this.medicineCharges = medicineCharges;
        this.otherCharges = otherCharges;
        this.total = calculateTotal(consultationFee, medicineCharges, otherCharges);
        this.status = status;
        this.billDate = billDate;
    }

    /** Adds the three charges and rounds to 2 decimals. */
    public static double calculateTotal(double consultation, double medicine, double other) {
        return Math.round((consultation + medicine + other) * 100.0) / 100.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public Integer getAppointmentId() { return appointmentId; }
    public String getPatientName() { return patientName; }
    public double getConsultationFee() { return consultationFee; }
    public double getMedicineCharges() { return medicineCharges; }
    public double getOtherCharges() { return otherCharges; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getBillDate() { return billDate; }
}
