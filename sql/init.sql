-- ============================================================
-- PROJETO UNIUBE: Sistema de Recomendações de Livros por Gênero
-- BANCO DE DADOS MODELO MVC (MySQL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_recomendador_livros
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE db_recomendador_livros;

-- ------------------------------------------------------------
-- 1. TABELA DE USUÁRIOS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. TABELA DE GÊNEROS LITERÁRIOS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS generos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. TABELA DE LIVROS (Alvo Principal do CRUD)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS livros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    isbn VARCHAR(20),
    ano_publicacao INT,
    genero_id INT NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_livros_generos FOREIGN KEY (genero_id)
        REFERENCES generos(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. TABELA DE PREFERÊNCIAS DO USUÁRIO (Relacionamento N:M)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_generos (
    usuario_id INT NOT NULL,
    genero_id INT NOT NULL,
    data_selecao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, genero_id),
    CONSTRAINT fk_ug_usuarios FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ug_generos FOREIGN KEY (genero_id)
        REFERENCES generos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. TABELA DE LOG/AUDITORIA (Para registrar ações do Trigger)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS log_alteracoes_livros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livro_id INT NOT NULL,
    acao VARCHAR(20) NOT NULL,
    titulo_anterior VARCHAR(150),
    titulo_novo VARCHAR(150),
    data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- VIEWS (Simplificam a camada DAO do Java)
-- ============================================================

CREATE OR REPLACE VIEW vw_livros_detalhados AS
SELECT 
    l.id AS livro_id,
    l.titulo,
    l.autor,
    l.isbn,
    l.ano_publicacao,
    g.id AS genero_id,
    g.nome AS genero_nome
FROM livros l
INNER JOIN generos g ON l.genero_id = g.id;

-- ============================================================
-- TRIGGERS (Auditoria Automática do CRUD)
-- ============================================================

DROP TRIGGER IF EXISTS trg_depois_atualizar_livro;
DELIMITER //
CREATE TRIGGER trg_depois_atualizar_livro
AFTER UPDATE ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_alteracoes_livros (livro_id, acao, titulo_anterior, titulo_novo)
    VALUES (OLD.id, 'UPDATE', OLD.titulo, NEW.titulo);
END;
//
DELIMITER ;

DROP TRIGGER IF EXISTS trg_depois_deletar_livro;
DELIMITER //
CREATE TRIGGER trg_depois_deletar_livro
AFTER DELETE ON livros
FOR EACH ROW
BEGIN
    INSERT INTO log_alteracoes_livros (livro_id, acao, titulo_anterior, titulo_novo)
    VALUES (OLD.id, 'DELETE', OLD.titulo, NULL);
END;
//
DELIMITER ;

-- ============================================================
-- STORED PROCEDURES (Lógica de Recomendação)
-- ============================================================

DROP PROCEDURE IF EXISTS sp_obter_recomendacoes_usuario;
DELIMITER //
CREATE PROCEDURE sp_obter_recomendacoes_usuario(IN p_usuario_id INT)
BEGIN
    SELECT DISTINCT 
        l.id AS livro_id,
        l.titulo,
        l.autor,
        l.ano_publicacao,
        g.nome AS genero_nome
    FROM livros l
    INNER JOIN generos g ON l.genero_id = g.id
    INNER JOIN usuario_generos ug ON ug.genero_id = g.id
    WHERE ug.usuario_id = p_usuario_id
    ORDER BY l.titulo ASC;
END;
//
DELIMITER ;

-- ============================================================
-- MASSA DE DADOS PARA TESTES (SEED DATA)
-- ============================================================

INSERT IGNORE INTO generos (id, nome, descricao) VALUES
(1, 'Ficção Científica', 'Histórias baseadas em ciência, tecnologia e futuro'),
(2, 'Fantasia', 'Mundos imaginários, magia e criaturas míticas'),
(3, 'Sistemas e Tecnologia', 'Livros técnicos de computação e engenharia de software'),
(4, 'Romance', 'Histórias focadas em relacionamentos e sentimentos'),
(5, 'Terror / Suspense', 'Histórias de mistério, suspense psicológico e horror');

INSERT IGNORE INTO usuarios (id, nome, email) VALUES
(1, 'Aluno ADS UNIUBE', 'aluno.ads@uniube.br'),
(2, 'Maria Oliveira', 'maria@email.com');

INSERT IGNORE INTO livros (id, titulo, autor, isbn, ano_publicacao, genero_id) VALUES
(1, 'Duna', 'Frank Herbert', '978-8576572008', 1965, 1),
(2, 'O Neuromancer', 'William Gibson', '978-8576573005', 1984, 1),
(3, 'O Senhor dos Anéis', 'J.R.R. Tolkien', '978-8595084742', 1954, 2),
(4, 'O Nome do Vento', 'Patrick Rothfuss', '978-8580410051', 2007, 2),
(5, 'Entendendo Algoritmos', 'Aditya Y. Bhargava', '978-8575225639', 2017, 3),
(6, 'Código Limpo (Clean Code)', 'Robert C. Martin', '978-8576082675', 2009, 3),
(7, 'Orgulho e Preconceito', 'Jane Austen', '978-8535902778', 1813, 4),
(8, 'O Iluminado', 'Stephen King', '978-8535914849', 1977, 5);

INSERT IGNORE INTO usuario_generos (usuario_id, genero_id) VALUES
(1, 1),
(1, 3),
(2, 2),
(2, 4);
