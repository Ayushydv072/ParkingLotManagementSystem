package service;

import db.DBManager;
import db.ParkingDatabase;
import model.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Central business-logic service for parking operations.
 */
public class ParkingService {

    private final ParkingDatabase db;

    public ParkingService() {
        this.db = ParkingDatabase.getInstance();
    }

    // ─── Entry ───────────────────────────────────────────────────────────────

    /**
     * Attempts to park a vehicle.
     * @return ParkingResult with success flag, ticket, or queue position.
     */
    public ParkingResult parkVehicle(Vehicle vehicle) {
        // Duplicate check
        ParkingTicket existing = db.findTicketByVehicleNumber(vehicle.getVehicleNumber());
        if (existing != null) {
            return ParkingResult.alreadyParked(existing);
        }

        Optional<ParkingSlot> slotOpt = db.findBestSlot(vehicle.getVehicleType());

        if (slotOpt.isEmpty()) {
            // Lot full – add to waiting queue
            db.addToWaitingQueue(vehicle);
            int pos = db.waitingQueueSize();
            return ParkingResult.queuedAt(pos);
        }

        ParkingSlot slot = slotOpt.get();
        slot.occupy(vehicle);

        ParkingTicket ticket = new ParkingTicket(vehicle, slot);
        db.addActiveTicket(ticket); 
        DBManager.insertTicket(ticket);
        DBManager.updateSlotStatus(slot.getSlotId(), "OCCUPIED");

        return ParkingResult.success(ticket);
    }

    // ─── Exit ────────────────────────────────────────────────────────────────

    /**
     * Processes checkout: computes fee and updates the ticket.
     */
    public CheckoutResult checkout(String identifier) {
        // Try by ticketId first, then by vehicle number
        ParkingTicket ticket = db.getActiveTicket(identifier);
        if (ticket == null) ticket = db.findTicketByVehicleNumber(identifier);
        if (ticket == null) return CheckoutResult.notFound();

        long durationMinutes = ticket.getDurationMinutes();
        PricingPolicy policy = db.getPricingPolicy(ticket.getVehicle().getVehicleType());
        double fee = policy.calculateFee(durationMinutes);

        ticket.setExitTime(LocalDateTime.now());
        ticket.setTotalFee(fee);
        return CheckoutResult.ok(ticket, fee, durationMinutes);
    }

    /**
     * Confirms payment and frees the slot.
     */
    public void confirmPayment(ParkingTicket ticket, String paymentMethod) {
        ticket.setPaymentMethod(paymentMethod);
        ticket.setStatus(ParkingTicket.TicketStatus.PAID);
        ticket.getSlot().vacate();
        db.closeTicket(ticket); 
        DBManager.updateTicketOnExit(ticket);
        DBManager.updateSlotStatus(ticket.getSlot().getSlotId(), "AVAILABLE");

        // Attempt to park next vehicle from waiting queue
        processWaitingQueue(ticket.getSlot().getSuitableFor());
    }

    private void processWaitingQueue(Vehicle.VehicleType freedType) {
        if (db.waitingQueueSize() == 0) return;
        Vehicle next = db.peekWaitingQueue();
        if (next != null && next.getVehicleType() == freedType) {
            db.pollWaitingQueue();
            parkVehicle(next);
        }
    }

    // ─── Lookups ─────────────────────────────────────────────────────────────

    public ParkingDatabase getDb() { return db; }

    // ─── Inner result classes ─────────────────────────────────────────────────

    public static class ParkingResult {
        public enum Type { SUCCESS, QUEUED, ALREADY_PARKED }

        public final Type type;
        public final ParkingTicket ticket;
        public final int queuePosition;

        private ParkingResult(Type t, ParkingTicket tk, int pos) {
            this.type = t; this.ticket = tk; this.queuePosition = pos;
        }

        static ParkingResult success(ParkingTicket t)     { return new ParkingResult(Type.SUCCESS, t, -1); }
        static ParkingResult queuedAt(int pos)            { return new ParkingResult(Type.QUEUED, null, pos); }
        static ParkingResult alreadyParked(ParkingTicket t){ return new ParkingResult(Type.ALREADY_PARKED, t, -1); }
    }

    public static class CheckoutResult {
        public final boolean found;
        public final ParkingTicket ticket;
        public final double fee;
        public final long durationMinutes;

        private CheckoutResult(boolean f, ParkingTicket t, double fee, long dur) {
            this.found = f; this.ticket = t; this.fee = fee; this.durationMinutes = dur;
        }

        static CheckoutResult ok(ParkingTicket t, double fee, long dur) { return new CheckoutResult(true, t, fee, dur); }
        static CheckoutResult notFound()                                 { return new CheckoutResult(false, null, 0, 0); }
    }
    
}
