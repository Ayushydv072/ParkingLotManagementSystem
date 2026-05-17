package db;

import model.*;
import java.sql.*;
import java.time.LocalDateTime;

public class DBManager {

    // ── Save ticket on entry ──────────────────────────────────────────────
    public static void insertTicket(ParkingTicket t) {
        String sql = "INSERT INTO tickets (ticket_id, vehicle_number, vehicle_type, " +
                     "owner_name, slot_id, entry_time, status) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getTicketId());
            ps.setString(2, t.getVehicle().getVehicleNumber());
            ps.setString(3, t.getVehicle().getVehicleType().name());
            ps.setString(4, t.getVehicle().getOwnerName());
            ps.setString(5, t.getSlot().getSlotId());
            ps.setTimestamp(6, Timestamp.valueOf(t.getEntryTime()));
            ps.setString(7, "ACTIVE");
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Update ticket on exit/payment ─────────────────────────────────────
    public static void updateTicketOnExit(ParkingTicket t) {
        String sql = "UPDATE tickets SET exit_time=?, total_fee=?, " +
                     "payment_method=?, status=? WHERE ticket_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(t.getExitTime()));
            ps.setDouble(2, t.getTotalFee());
            ps.setString(3, t.getPaymentMethod());
            ps.setString(4, t.getStatus().name());
            ps.setString(5, t.getTicketId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Update slot status ────────────────────────────────────────────────
    public static void updateSlotStatus(String slotId, String status) {
        String sql = "UPDATE slots SET status=? WHERE slot_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, slotId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Load all slots into DB on first run ───────────────────────────────
    public static void initSlotsIfEmpty() {
        try {
            Statement st = DBConnection.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM slots");
            rs.next();
            if (rs.getInt(1) == 0) {
                for (int i = 1; i <= 10; i++) insertSlot(String.format("TW-%02d", i), "TWO_WHEELER");
                for (int i = 1; i <= 8;  i++) insertSlot(String.format("FW-%02d", i), "FOUR_WHEELER");
                for (int i = 1; i <= 4;  i++) insertSlot(String.format("HV-%02d", i), "HEAVY_VEHICLE");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void insertSlot(String id, String type) {
        String sql = "INSERT IGNORE INTO slots VALUES (?, ?, 'AVAILABLE')";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, type);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Load today's revenue ──────────────────────────────────────────────
    public static double getTodayRevenue() {
        String sql = "SELECT SUM(total_fee) FROM tickets " +
                     "WHERE DATE(exit_time) = CURDATE() AND status='PAID'";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Load today's vehicle count ────────────────────────────────────────
    public static int getTodayVehicleCount() {
        String sql = "SELECT COUNT(*) FROM tickets " +
                     "WHERE DATE(exit_time) = CURDATE() AND status='PAID'";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}