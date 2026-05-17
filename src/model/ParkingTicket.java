package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ParkingTicket {
    public enum TicketStatus { ACTIVE, PAID, CANCELLED }

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSlot slot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double totalFee;
    private String paymentMethod;
    private TicketStatus status;

    public ParkingTicket(Vehicle vehicle, ParkingSlot slot) {
        this.ticketId    = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.vehicle     = vehicle;
        this.slot        = slot;
        this.entryTime   = vehicle.getEntryTime();
        this.status      = TicketStatus.ACTIVE;
        this.totalFee    = 0.0;
        this.paymentMethod = "";
    }

    public String getTicketId()           { return ticketId; }
    public Vehicle getVehicle()           { return vehicle; }
    public ParkingSlot getSlot()          { return slot; }
    public LocalDateTime getEntryTime()   { return entryTime; }
    public LocalDateTime getExitTime()    { return exitTime; }
    public double getTotalFee()           { return totalFee; }
    public String getPaymentMethod()      { return paymentMethod; }
    public TicketStatus getStatus()       { return status; }

    public void setExitTime(LocalDateTime exitTime)   { this.exitTime = exitTime; }
    public void setTotalFee(double fee)               { this.totalFee = fee; }
    public void setPaymentMethod(String method)       { this.paymentMethod = method; }
    public void setStatus(TicketStatus status)        { this.status = status; }

    /** Duration in minutes */
    public long getDurationMinutes() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return java.time.Duration.between(entryTime, end).toMinutes();
    }
}
