package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        List<Mechanic> results = viewLoader.loadViewWithCondition("vw_mechanics", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Mechanic> findAll() throws SQLException {
        return viewLoader.loadView("vw_mechanics");
    }

    public List<Mechanic> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_mechanics", "workshop_id = ? ORDER BY name", workshopId);
    }

    public List<Mechanic> findBySpecialization(String specialization) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_mechanics", "specialization ILIKE ? ORDER BY name", "%" + specialization + "%");
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