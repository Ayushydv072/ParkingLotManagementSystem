package model;

public class ParkingSlot {
    public enum SlotStatus { AVAILABLE, OCCUPIED }

    private String slotId;
    private Vehicle.VehicleType suitableFor;
    private SlotStatus status;
    private Vehicle occupiedBy;

    public ParkingSlot(String slotId, Vehicle.VehicleType suitableFor) {
        this.slotId      = slotId;
        this.suitableFor = suitableFor;
        this.status      = SlotStatus.AVAILABLE;
        this.occupiedBy  = null;
    }

    public String getSlotId()                     { return slotId; }
    public Vehicle.VehicleType getSuitableFor()   { return suitableFor; }
    public SlotStatus getStatus()                 { return status; }
    public Vehicle getOccupiedBy()                { return occupiedBy; }
    public boolean isAvailable()                  { return status == SlotStatus.AVAILABLE; }

    public void occupy(Vehicle v) {
        this.occupiedBy = v;
        this.status     = SlotStatus.OCCUPIED;
    }

    public void vacate() {
        this.occupiedBy = null;
        this.status     = SlotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return slotId + " [" + suitableFor + "] - " + status;
    }
}
