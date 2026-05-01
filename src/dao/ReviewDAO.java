package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.CustomerReview;

public class ReviewDAO extends BaseDAO<CustomerReview> {

    @Override
    public CustomerReview findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<CustomerReview> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE customer_id = ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByCustomerName(String customerName) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE customer_name ILIKE ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + customerName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE workshop_id = ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByWorkshopName(String workshopName) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE workshop_name ILIKE ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + workshopName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByRating(int rating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating = ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, rating);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findHighRatedReviews(int minRating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating >= ? ORDER BY rating DESC, review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, minRating);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findLowRatedReviews(int maxRating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating <= ? ORDER BY rating ASC, review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maxRating);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerReview> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE review_date BETWEEN ? AND ? ORDER BY review_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerReview> reviews = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, startDate);
            ps.setObject(2, endDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            return reviews;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(CustomerReview entity) throws SQLException {
        String sql = "CALL sp_submit_review(?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_review(?, ?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getWorkshopId());
            cs.setInt(3, entity.getRating());
            cs.setString(4, entity.getReviewText());
            cs.execute();
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public int insertAndGetId(CustomerReview entity) throws SQLException {
        String sql = "CALL sp_submit_review(?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_review(?, ?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getWorkshopId());
            cs.setInt(3, entity.getRating());
            cs.setString(4, entity.getReviewText());
            cs.execute();

            String querySql = "SELECT id FROM customer_reviews WHERE customer_id = ? ORDER BY review_date DESC LIMIT 1";
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, entity.getCustomerId());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            closeResources(rs, ps, null);
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    @Override
    public boolean update(CustomerReview entity) throws SQLException {
        String sql = "UPDATE customer_reviews SET rating = ?, review_text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entity.getRating());
            ps.setString(2, entity.getReviewText());
            ps.setInt(3, entity.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_reviews WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public boolean deleteByWorkshopId(int workshopId) throws SQLException {
        String sql = "DELETE FROM customer_reviews WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public double getAverageRatingByWorkshop(int workshopId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(rating), 0) FROM customer_reviews WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<WorkshopRatingSummary> getWorkshopRatingSummaries() throws SQLException {
        String sql = "SELECT workshop_id, workshop_name, COUNT(*) as review_count, AVG(rating) as avg_rating, " +
                "MIN(rating) as min_rating, MAX(rating) as max_rating " +
                "FROM vw_customer_reviews GROUP BY workshop_id, workshop_name ORDER BY avg_rating DESC";

        List<WorkshopRatingSummary> summaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                WorkshopRatingSummary summary = new WorkshopRatingSummary();
                summary.workshopId = rs.getInt("workshop_id");
                summary.workshopName = rs.getString("workshop_name");
                summary.reviewCount = rs.getInt("review_count");
                summary.averageRating = rs.getDouble("avg_rating");
                summary.minRating = rs.getInt("min_rating");
                summary.maxRating = rs.getInt("max_rating");
                summaries.add(summary);
            }

            return summaries;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public static class WorkshopRatingSummary {
        public int workshopId;
        public String workshopName;
        public int reviewCount;
        public double averageRating;
        public int minRating;
        public int maxRating;
    }

    @Override
    protected CustomerReview mapRow(ResultSet rs) throws SQLException {
        CustomerReview review = new CustomerReview();
        review.setId(rs.getInt("id"));
        review.setCustomerId(rs.getInt("customer_id"));
        review.setCustomerName(rs.getString("customer_name"));
        review.setWorkshopId(rs.getInt("workshop_id"));
        review.setWorkshopName(rs.getString("workshop_name"));
        review.setRating(rs.getInt("rating"));
        review.setReviewText(rs.getString("review_text"));

        if (rs.getTimestamp("review_date") != null) {
            review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return review;
    }
}