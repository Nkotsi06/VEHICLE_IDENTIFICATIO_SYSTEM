package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Mechanic;

/**
 * MechanicDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class MechanicDAO extends BaseDAO<Mechanic> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public MechanicDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Mechanic findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_mechanics", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToMechanic(results.get(0));
    }

    @Override
    public List<Mechanic> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_mechanics");
        return mapMapsToMechanics(results);
    }

    public List<Mechanic> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_mechanics", "workshop_id = ? ORDER BY name", workshopId);
        return mapMapsToMechanics(results);
    }

    public List<Mechanic> findBySpecialization(String specialization) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_mechanics", "specialization ILIKE ? ORDER BY name", "%" + specialization + "%");
        return mapMapsToMechanics(results);
    }

    public int countByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_mechanics", "workshop_id = ?", workshopId);
    }

    @Override
    public boolean insert(Mechanic entity) throws SQLException {
        Integer mechanicId = procedureCaller.executeAddMechanic(
                entity.getWorkshopId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhone()
        );
        if (mechanicId != null && mechanicId > 0) {
            entity.setId(mechanicId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(Mechanic entity) throws SQLException {
        Integer mechanicId = procedureCaller.executeAddMechanic(
                entity.getWorkshopId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhone()
        );
        if (mechanicId != null && mechanicId > 0) {
            entity.setId(mechanicId);
            return mechanicId;
        }
        return -1;
    }

    @Override
    public boolean update(Mechanic entity) throws SQLException {
        return procedureCaller.executeUpdateMechanic(
                entity.getId(),
                entity.getWorkshopId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhone()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteMechanic(id);
    }

    /**
     * Converts a List of Maps to a List of Mechanic objects.
     */
    private List<Mechanic> mapMapsToMechanics(List<Map<String, Object>> maps) {
        List<Mechanic> mechanics = new ArrayList<>();
        if (maps == null) {
            return mechanics;
        }
        for (Map<String, Object> map : maps) {
            Mechanic mechanic = mapMapToMechanic(map);
            if (mechanic != null) {
                mechanics.add(mechanic);
            }
        }
        return mechanics;
    }

    /**
     * Converts a Map to a Mechanic object.
     */
    private Mechanic mapMapToMechanic(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Mechanic mechanic = new Mechanic();

        mechanic.setId(getIntValue(map, "id"));
        mechanic.setWorkshopId(getIntValue(map, "workshop_id"));
        mechanic.setWorkshopName(getStringValue(map, "workshop_name"));
        mechanic.setName(getStringValue(map, "name"));
        mechanic.setSpecialization(getStringValue(map, "specialization"));
        mechanic.setPhone(getStringValue(map, "phone"));

        mechanic.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        mechanic.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return mechanic;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }

    @Override
    protected Mechanic mapRow(ResultSet rs) throws SQLException {
        Mechanic mechanic = new Mechanic();
        mechanic.setId(rs.getInt("id"));
        mechanic.setWorkshopId(rs.getInt("workshop_id"));
        mechanic.setWorkshopName(rs.getString("workshop_name"));
        mechanic.setName(rs.getString("name"));
        mechanic.setSpecialization(rs.getString("specialization"));
        mechanic.setPhone(rs.getString("phone"));

        if (rs.getTimestamp("created_at") != null) {
            mechanic.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            mechanic.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return mechanic;
    }
}