package model;

public class PricingPolicy {
    private Vehicle.VehicleType vehicleType;
    private double baseCharge;       // fixed initial charge
    private double hourlyRate;       // per hour after first hour
    private double penaltyPerHour;   // overnight / extended stay penalty

    public PricingPolicy(Vehicle.VehicleType type, double baseCharge,
                         double hourlyRate, double penaltyPerHour) {
        this.vehicleType    = type;
        this.baseCharge     = baseCharge;
        this.hourlyRate     = hourlyRate;
        this.penaltyPerHour = penaltyPerHour;
    }

    public Vehicle.VehicleType getVehicleType() { return vehicleType; }
    public double getBaseCharge()               { return baseCharge; }
    public double getHourlyRate()               { return hourlyRate; }
    public double getPenaltyPerHour()           { return penaltyPerHour; }

    public void setBaseCharge(double v)      { this.baseCharge = v; }
    public void setHourlyRate(double v)      { this.hourlyRate = v; }
    public void setPenaltyPerHour(double v)  { this.penaltyPerHour = v; }

    /**
     * Calculates fee for a given duration in minutes.
     * Logic: base + hourly for each started hour beyond the first free hour.
     * Penalty applies per hour beyond 12 hours.
     */
    public double calculateFee(long durationMinutes) {
        double fee = baseCharge;
        if (durationMinutes <= 0) return fee;

        long hours = (long) Math.ceil(durationMinutes / 60.0);

        if (hours <= 1) return fee;   // first hour free (base only)

        long billableHours = hours - 1;
        fee += billableHours * hourlyRate;

        // penalty for stay beyond 12 hours
        if (hours > 12) {
            long penaltyHours = hours - 12;
            fee += penaltyHours * penaltyPerHour;
        }
        return fee;
    }
}
