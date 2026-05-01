package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.Mechanic;

public class MechanicDAO extends BaseDAO<Mechanic> {

    @Override
    public Mechanic findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_mechanics WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<Mechanic> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_mechanics ORDER BY name";
        return executeQuery(sql);
    }

    public List<Mechanic> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_mechanics WHERE workshop_id = ? ORDER BY name";
        return executeQuery(sql, workshopId);
    }

    public List<Mechanic> findBySpecialization(String specialization) throws SQLException {
        String sql = "SELECT * FROM vw_mechanics WHERE specialization ILIKE ? ORDER BY name";
        return executeQuery(sql, "%" + specialization + "%");
    }

    public int countByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mechanics WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(Mechanic entity) throws SQLException {
        return executeProcedure("sp_add_mechanic",
                entity.getWorkshopId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhone()
        );
    }

    @Override
    public boolean update(Mechanic entity) throws SQLException {
        return executeProcedure("sp_update_mechanic",
                entity.getId(),
                entity.getWorkshopId(),
                entity.getName(),
                entity.getSpecialization(),
                entity.getPhone()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_mechanic", id);
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