package com.bluemoon.services.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bluemoon.models.Phong;
import com.bluemoon.services.PhongService;
import com.bluemoon.utils.DatabaseConnector;

public class PhongServiceImpl implements PhongService {
    
    private static final Logger LOGGER = Logger.getLogger(PhongServiceImpl.class.getName());

    @Override
    public List<Phong> getAllPhong() {
        List<Phong> list = new ArrayList<>();
        String sql = "SELECT * FROM Phong ORDER BY soPhong ASC";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Phong p = new Phong();
                p.setSoPhong(rs.getInt("soPhong"));
                p.setDienTich(rs.getBigDecimal("dienTich"));
                p.setGiaTien(rs.getBigDecimal("giaTien"));
                p.setTrangThai(rs.getString("trangThai"));
                p.setGhiChu(rs.getString("ghiChu"));
                list.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getAllPhong", e);
        }
        return list;
    }

    @Override
    public Phong getPhongById(int soPhong) {
        String sql = "SELECT * FROM Phong WHERE soPhong = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, soPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Phong(
                        rs.getInt("soPhong"),
                        rs.getBigDecimal("dienTich"),
                        rs.getBigDecimal("giaTien"),
                        rs.getString("trangThai"),
                        rs.getString("ghiChu")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi getPhongById: " + soPhong, e);
        }
        return null;
    }

    @Override
    public boolean checkSoPhongExists(int soPhong) {
        return getPhongById(soPhong) != null;
    }

    @Override
    public boolean addPhong(Phong phong) {
        if (checkSoPhongExists(phong.getSoPhong())) {
            LOGGER.warning("Số phòng " + phong.getSoPhong() + " đã tồn tại.");
            return false;
        }

        String sql = "INSERT INTO Phong (soPhong, dienTich, giaTien, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, phong.getSoPhong());
            ps.setBigDecimal(2, phong.getDienTich());
            ps.setBigDecimal(3, phong.getGiaTien());
            ps.setString(4, phong.getTrangThai());
            ps.setString(5, phong.getGhiChu());
            
            return ps.executeUpdate() > 0; // Trả về true nếu insert thành công
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi addPhong", e);
            return false;
        }
    }

    @Override
    public boolean updatePhong(Phong phong) {

        // Kiểm tra trạng thái hiện tại trong DB
        Phong currentRoom = getPhongById(phong.getSoPhong());
        if (currentRoom != null && "DangO".equals(currentRoom.getTrangThai())) {
            // Nếu đang ở -> Không cho sửa
            return false; 
        }

        String sql = "UPDATE Phong SET dienTich = ?, giaTien = ?, trangThai = ?, ghiChu = ? WHERE soPhong = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBigDecimal(1, phong.getDienTich());
            ps.setBigDecimal(2, phong.getGiaTien());
            ps.setString(3, phong.getTrangThai());
            ps.setString(4, phong.getGhiChu());
            ps.setInt(5, phong.getSoPhong());
            
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi updatePhong", e);
            return false;
        }
    }

    @Override
    public boolean deletePhong(int soPhong) {

        // Kiểm tra trạng thái
        Phong currentRoom = getPhongById(soPhong);
        if (currentRoom != null && "DangO".equals(currentRoom.getTrangThai())) {
             // Nếu đang ở -> Không cho xóa
            return false;
        }
        
        String sql = "DELETE FROM Phong WHERE soPhong = ?";
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, soPhong);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi deletePhong (có thể do ràng buộc khóa ngoại)", e);
            return false;
        }
    }
}