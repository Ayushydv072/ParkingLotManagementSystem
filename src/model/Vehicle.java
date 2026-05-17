package model;

import java.time.LocalDateTime;

public class Vehicle {
    public enum VehicleType {
        TWO_WHEELER, FOUR_WHEELER, HEAVY_VEHICLE
    }

    private String vehicleNumber;
    private VehicleType vehicleType;
    private String ownerName;
    private LocalDateTime entryTime;

    public Vehicle(String vehicleNumber, VehicleType vehicleType, String ownerName) {
        this.vehicleNumber = vehicleNumber.toUpperCase().trim();
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.entryTime = LocalDateTime.now();
    }

    public String getVehicleNumber()    { return vehicleNumber; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getOwnerName()        { return ownerName; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime t) { this.entryTime = t; }

    @Override
    public String toString() {
        return vehicleNumber + " (" + vehicleType + ")";
    }
}
