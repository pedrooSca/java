CREATE TABLE IF NOT EXISTS contas_acesso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil ENUM('ADMIN', 'CONSULTA') NOT NULL DEFAULT 'CONSULTA'
) ENGINE=InnoDB;

INSERT INTO generos (nome, descricao) VALUES
('Ficção Científica', 'Ciência, tecnologia e futuros possíveis'),
('Fantasia', 'Mundos imaginários e aventuras'),
('Romance', 'Histórias e relações humanas')
ON DUPLICATE KEY UPDATE nome = VALUES(nome);
