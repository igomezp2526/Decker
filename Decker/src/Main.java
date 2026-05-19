import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Clase principal que arranca la aplicación.
 * <p>
 * Utiliza los siguientes componentes Swing:
 * <ul>
 *   <li>{@link JFrame}: ventana principal de la aplicación.</li>
 *   <li>{@link JOptionPane}: cuadro de diálogo para pedir el nombre del jugador y
 *       mostrar el ranking.</li>
 *   <li>{@link JButton}: botones para las acciones del juego (Descartar, Jugar,
 *       Ver Ranking, Salir).</li>
 *   <li>{@link JPanel}: panel auxiliar para colocar los botones en la parte inferior.</li>
 *   <li>{@link BorderLayout}: gestor de colocación que permite poner el panel de juego en
 *       el centro y los botones abajo.</li>
 *   <li>{@link ActionListener}: gestiona los clics en los botones.</li>
 *   <li>{@link WindowAdapter}: cierra la conexión a la base de datos al cerrar la ventana.</li>
 * </ul>
 * <p>
 * El flujo es: pedir nombre, conectar a BD, crear ventana, añadir el panel de juego y
 * los botones, y mostrar la ventana.
 */
public class Main {
    /**
     * Punto de entrada de la aplicación.
     * @param args No se utilizan
     */
    public static void main(String[] args) {
        String nombre = JOptionPane.showInputDialog(null, "Introduce tu nombre de jugador:");
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Anónimo";
        }

        DatabaseManager dbManager = new DatabaseManager();

        JFrame ventana = new JFrame("Decker - Poker Minimalista");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);

        GamePanel gamePanel = new GamePanel(nombre, dbManager);

        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(Color.BLACK);

        JButton btnDescartar = new JButton("Descartar");
        JButton btnJugar = new JButton("Jugar");
        JButton btnRanking = new JButton("Ver Ranking");
        JButton btnSalir = new JButton("Salir");
        btnSalir.setVisible(false);

        btnDescartar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.descartar();
            }
        });

        btnJugar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.jugarMano();
            }
        });

        btnRanking.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> ranking = dbManager.obtenerRanking();
                StringBuilder sb = new StringBuilder("TOP 10 PUNTUACIONES\n\n");
                if (ranking.isEmpty()) {
                    sb.append("Sin datos.");
                } else {
                    for (String linea : ranking) {
                        sb.append(linea).append("\n");
                    }
                }
                JOptionPane.showMessageDialog(ventana, sb.toString(), "Ranking",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dbManager.cerrar();
                System.exit(0);
            }
        });

        gamePanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("juegoTerminado".equals(evt.getPropertyName()) && (Boolean) evt.getNewValue()) {
                    btnSalir.setVisible(true);
                }
            }
        });

        panelBotones.add(btnDescartar);
        panelBotones.add(btnJugar);
        panelBotones.add(btnRanking);
        panelBotones.add(btnSalir);

        ventana.setLayout(new BorderLayout());
        ventana.add(gamePanel, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);

        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                dbManager.cerrar();
                System.exit(0);
            }
        });
    }
}