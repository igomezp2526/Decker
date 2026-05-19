CREATE DATABASE decker;
USE decker;

CREATE TABLE partides (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_jugador VARCHAR(50) NOT NULL,
    puntuacio INT NOT NULL,
    nivell_max INT NOT NULL,
    data_partida DATETIME NOT NULL
);