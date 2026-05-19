import java.sql.*;
import java.util.ArrayList;

/**
 * Gestiona la conexión con la base de datos MySQL y las operaciones de inserción y consulta
 * de partidas para el ranking.
 */
public class DatabaseManager {
    /** URL de conexión a la base de datos 'decker' */
    private static final String URL = "jdbc:mysql://localhost:3306/decker?useSSL=false&serverTimezone=UTC";
    /** Usuario de la base de datos */
    private static final String USER = "decker_usr";
    /** Contraseña de la base de datos (vacía por defecto en XAMPP) */
    private static final String PASSWORD = "4NL9__uo_jgE!p1r_n";

    /** Objeto de conexión con MySQL */
    private Connection conexion;

    /**
     * Abre la conexión con la base de datos.
     * Si el driver no se encuentra o la conexión falla, muestra un mensaje de error.
     */
    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conectado a la base de datos.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: no se encontró el driver JDBC. Asegúrate de tener el .jar en el classpath.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos. Revisa URL, usuario y contraseña.");
        }
    }

    /**
     * Inserta una partida en la tabla 'partides' con los datos del jugador.
     * @param nombreJugador  Nombre del jugador
     * @param puntuacionTotal Puntuación total acumulada
     * @param nivelMaximo     Nivel máximo alcanzado
     */
    public void insertarPartida(String nombreJugador, int puntuacionTotal, int nivelMaximo) {
        String sql = "INSERT INTO partides (nom_jugador, puntuacio, nivell_max, data_partida) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombreJugador);
            stmt.setInt(2, puntuacionTotal);
            stmt.setInt(3, nivelMaximo);
            stmt.executeUpdate();
            System.out.println("Partida guardada.");
        } catch (SQLException e) {
            System.out.println("Error al guardar la partida.");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene las 10 mejores puntuaciones de la tabla 'partides' ordenadas de mayor a menor.
     * @return Lista de cadenas con la información del ranking
     */
    public ArrayList<String> obtenerRanking() {
        ArrayList<String> ranking = new ArrayList<>();
        String sql = "SELECT nom_jugador, puntuacio, nivell_max, data_partida FROM partides ORDER BY puntuacio DESC LIMIT 10";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String linea = rs.getString("nom_jugador") + " - " +
                        rs.getInt("puntuacio") + " pts (nivel " +
                        rs.getInt("nivell_max") + ") " +
                        rs.getTimestamp("data_partida");
                ranking.add(linea);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranking;
    }

    /**
     * Cierra la conexión con la base de datos.
     */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}