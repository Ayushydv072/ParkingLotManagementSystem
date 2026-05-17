package db;

import model.*;
import java.util.*;
import java.time.LocalDateTime;

/**
 * In-memory database for the parking system.
 * Acts as the single source of truth for all data.
 */
public class ParkingDatabase {

    private static ParkingDatabase instance;

    // Slots: slotId -> ParkingSlot
    private final Map<String, ParkingSlot> slots = new LinkedHashMap<>();

    // Active tickets: ticketId -> ParkingTicket
    private final Map<String, ParkingTicket> activeTickets = new LinkedHashMap<>();

    // Historical tickets (paid/cancelled)
    private final List<ParkingTicket> history = new ArrayList<>();

    // Pricing policies: vehicleType -> PricingPolicy
    private final Map<Vehicle.VehicleType, PricingPolicy> pricingPolicies = new HashMap<>();

    // Waiting queue when lot is full
    private final Queue<Vehicle> waitingQueue = new LinkedList<>();

    // Revenue tracking
    private double totalRevenue = 0.0;

    private ParkingDatabase() {
        initSlots();
        initPricing();
    }

    public static synchronized ParkingDatabase getInstance() {
        if (instance == null) instance = new ParkingDatabase();
        return instance;
    }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void initSlots() {
        // 10 two-wheeler slots: TW-01 … TW-10
        for (int i = 1; i <= 10; i++) {
            String id = String.format("TW-%02d", i);
            slots.put(id, new ParkingSlot(id, Vehicle.VehicleType.TWO_WHEELER));
        }
        // 8 four-wheeler slots: FW-01 … FW-08
        for (int i = 1; i <= 8; i++) {
            String id = String.format("FW-%02d", i);
            slots.put(id, new ParkingSlot(id, Vehicle.VehicleType.FOUR_WHEELER));
        }
        // 4 heavy vehicle slots: HV-01 … HV-04
        for (int i = 1; i <= 4; i++) {
            String id = String.format("HV-%02d", i);
            slots.put(id, new ParkingSlot(id, Vehicle.VehicleType.HEAVY_VEHICLE));
        }
    }

    private void initPricing() {
        pricingPolicies.put(Vehicle.VehicleType.TWO_WHEELER,
                new PricingPolicy(Vehicle.VehicleType.TWO_WHEELER, 10, 5, 2));
        pricingPolicies.put(Vehicle.VehicleType.FOUR_WHEELER,
                new PricingPolicy(Vehicle.VehicleType.FOUR_WHEELER, 20, 15, 5));
        pricingPolicies.put(Vehicle.VehicleType.HEAVY_VEHICLE,
                new PricingPolicy(Vehicle.VehicleType.HEAVY_VEHICLE, 50, 30, 10));
    }

    // ─── Slots ───────────────────────────────────────────────────────────────

    public Collection<ParkingSlot> getAllSlots() { return slots.values(); }

    public List<ParkingSlot> getAvailableSlots(Vehicle.VehicleType type) {
        List<ParkingSlot> result = new ArrayList<>();
        for (ParkingSlot s : slots.values())
            if (s.isAvailable() && s.getSuitableFor() == type)
                result.add(s);
        return result;
    }

    public ParkingSlot getSlotById(String slotId) { return slots.get(slotId); }

    /** Nearest-first: returns first available slot for the vehicle type */
    public Optional<ParkingSlot> findBestSlot(Vehicle.VehicleType type) {
        return getAvailableSlots(type).stream().findFirst();
    }

    public int countAvailable(Vehicle.VehicleType type) {
        return (int) slots.values().stream()
                .filter(s -> s.isAvailable() && s.getSuitableFor() == type).count();
    }

    public int countOccupied(Vehicle.VehicleType type) {
        return (int) slots.values().stream()
                .filter(s -> !s.isAvailable() && s.getSuitableFor() == type).count();
    }

    // ─── Tickets ─────────────────────────────────────────────────────────────

    public void addActiveTicket(ParkingTicket t) { activeTickets.put(t.getTicketId(), t); }

    public ParkingTicket getActiveTicket(String ticketId) { return activeTickets.get(ticketId); }

    public Collection<ParkingTicket> getAllActiveTickets() { return activeTickets.values(); }

    public ParkingTicket findTicketByVehicleNumber(String vehicleNumber) {
        for (ParkingTicket t : activeTickets.values())
            if (t.getVehicle().getVehicleNumber().equalsIgnoreCase(vehicleNumber))
                return t;
        return null;
    }

    public void closeTicket(ParkingTicket t) {
        activeTickets.remove(t.getTicketId());
        history.add(t);
        totalRevenue += t.getTotalFee();
    }

    public List<ParkingTicket> getHistory() { return Collections.unmodifiableList(history); }

    // ─── Pricing ─────────────────────────────────────────────────────────────

    public PricingPolicy getPricingPolicy(Vehicle.VehicleType type) { return pricingPolicies.get(type); }
    public Collection<PricingPolicy> getAllPolicies() { return pricingPolicies.values(); }

    // ─── Queue ───────────────────────────────────────────────────────────────

    public void addToWaitingQueue(Vehicle v) { waitingQueue.add(v); }
    public Vehicle peekWaitingQueue()        { return waitingQueue.peek(); }
    public Vehicle pollWaitingQueue()        { return waitingQueue.poll(); }
    public int waitingQueueSize()            { return waitingQueue.size(); }
    public Queue<Vehicle> getWaitingQueue()  { return waitingQueue; }

    // ─── Revenue ─────────────────────────────────────────────────────────────

    public double getTotalRevenue() { return totalRevenue; }

    public double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return history.stream()
                .filter(t -> t.getExitTime() != null && t.getExitTime().isAfter(startOfDay))
                .mapToDouble(ParkingTicket::getTotalFee).sum();
    }

    public int getTotalVehiclesServedToday() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return (int) history.stream()
                .filter(t -> t.getExitTime() != null && t.getExitTime().isAfter(startOfDay))
                .count();
    }
}
