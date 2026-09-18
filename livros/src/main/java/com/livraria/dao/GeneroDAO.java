package com.livraria.dao;

import com.livraria.model.Genero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gerenciamento da tabela 'generos'.
 */
public class GeneroDAO extends MysqlDAO {

    public List<Genero> listarTodos() throws SQLException {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT id, nome, descricao FROM generos ORDER BY nome ASC";

        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Genero(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao")
                ));
            }
        }
        return lista;
    }

    public Optional<Genero> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, descricao FROM generos WHERE id = ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Genero(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public Genero inserir(Genero genero) throws SQLException {
        String sql = "INSERT INTO generos (nome, descricao) VALUES (?, ?)";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, genero.getNome());
            ps.setString(2, genero.getDescricao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    genero.setId(rs.getInt(1));
                }
            }
        }
        return genero;
    }
}
