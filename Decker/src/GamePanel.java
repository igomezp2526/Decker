import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Panel principal del juego, que extiende {@link JPanel} para dibujar la interfaz.
 * <p>
 * Utiliza los siguientes elementos de Swing:
 * <ul>
 *   <li>{@link JPanel}: contenedor donde se dibujan las cartas y la información.</li>
 *   <li>{@link Graphics2D}: contexto gráfico para dibujar rectángulos, texto y colores.</li>
 *   <li>{@link MouseListener}: gestiona los clics del ratón para seleccionar cartas.</li>
 *   <li>{@link PropertyChangeSupport}: permite notificar a otros componentes (como Main)
 *       cuando el juego termina, para mostrar el botón de salir.</li>
 * </ul>
 * <p>
 * El dibujo se realiza en el método {@link #paintComponent(Graphics)}, que es llamado
 * automáticamente por Swing cuando es necesario repintar. Las cartas se representan como
 * rectángulos redondeados con texto. El fondo de cada carta es blanco, y el texto es rojo
 * para corazones y diamantes, negro para tréboles y picas. La selección se indica con un
 * sombreado cian semitransparente.
 * <p>
 * La ordenación de las cartas sigue el estilo Balatro: As a la izquierda, luego K, Q, J, 10...2.
 * <p>
 * Restricciones:
 * <ul>
 *   <li>Solo se pueden seleccionar un máximo de 5 cartas a la vez.</li>
 *   <li>Para jugar una mano es obligatorio tener exactamente 5 cartas seleccionadas.</li>
 *   <li>Para descartar se pueden seleccionar de 1 a 5 cartas.</li>
 * </ul>
 */
public class GamePanel extends JPanel implements MouseListener {

    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;
    private static final int MARGIN = 10;
    private static final int MAX_SELECCION = 5;

    private Baraja baraja;
    private Carta[] mano;
    private boolean[] seleccionadas;
    private int manosRestantes;
    private int descartesRestantes;
    private int nivelActual;
    private int puntuacionAcumulada;
    private int puntuacionTotal;
    private String mensajeEstado;

    /** Indica si la partida ha terminado (por victoria, derrota o sin manos). */
    public boolean juegoTerminado;

    private DatabaseManager dbManager;
    private String nombreJugador;

    /** Soporte para notificar cambios de propiedad a otros objetos (ej. Main). */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Objetivos de puntuación para cada uno de los 3 niveles. */
    private final int[] OBJETIVOS = {50, 100, 150};

    /**
     * Construye el panel del juego e inicializa el primer nivel.
     * <p>
     * Se establece el tamaño preferido del panel, el color de fondo oscuro, y se registra
     * este mismo objeto como {@link MouseListener} para detectar clics en las cartas.
     *
     * @param nombreJugador Nombre que introdujo el usuario al inicio
     * @param dbManager     Gestor de base de datos para guardar la partida
     */
    public GamePanel(String nombreJugador, DatabaseManager dbManager) {
        this.nombreJugador = nombreJugador;
        this.dbManager = dbManager;
        baraja = new Baraja();
        mano = new Carta[8];
        seleccionadas = new boolean[8];
        nivelActual = 1;
        puntuacionAcumulada = 0;
        puntuacionTotal = 0;
        mensajeEstado = "Nivel 1 - Objetivo: " + OBJETIVOS[0] + " puntos";
        juegoTerminado = false;

        iniciarNivel();

        this.setPreferredSize(new Dimension(1000, 700));
        this.setBackground(new Color(0x0A0A0A));
        this.addMouseListener(this);
        this.setFocusable(true);
    }

    /**
     * Registra un {@link PropertyChangeListener} externo para ser notificado de cambios en
     * propiedades del panel (como "juegoTerminado").
     * @param listener Objeto que quiere escuchar cambios
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Prepara el nivel actual: roba 8 cartas, reinicia manos y descartes, y ordena la mano.
     */
    public void iniciarNivel() {
        if (baraja.cartasRestantes() < 16) {
            baraja.reiniciar();
        }
        for (int i = 0; i < 8; i++) {
            mano[i] = baraja.robarCarta();
        }
        for (int i = 0; i < 8; i++) {
            seleccionadas[i] = false;
        }
        ordenarMano();

        manosRestantes = 5;
        descartesRestantes = 3;
        mensajeEstado = "Nivel " + nivelActual + " | Objetivo: " + OBJETIVOS[nivelActual-1]
                + " pts | Manos: " + manosRestantes + " | Descartas: " + descartesRestantes;
        repaint();
    }

    /**
     * Juega la mano formada por las 5 cartas que deben estar seleccionadas.
     * <p>
     * Requisitos:
     * <ul>
     *   <li>Debe haber exactamente {@value MAX_SELECCION} cartas seleccionadas.</li>
     *   <li>Deben quedar manos disponibles.</li>
     *   <li>El juego no debe haber terminado.</li>
     * </ul>
     * Si no se cumple el primer requisito, se muestra un mensaje y no se gasta la mano.
     * <p>
     * Tras evaluar, las cartas usadas se reemplazan por nuevas del mazo y la selección se limpia.
     */
    public void jugarMano() {
        if (juegoTerminado || manosRestantes <= 0) return;

        int selecCount = 0;
        for (boolean b : seleccionadas) if (b) selecCount++;

        if (selecCount != MAX_SELECCION) {
            mensajeEstado = "Debes seleccionar exactamente " + MAX_SELECCION + " cartas para jugar.";
            repaint();
            return;
        }

        Carta[] manoJugada = new Carta[MAX_SELECCION];
        int idx = 0;
        for (int i = 0; i < mano.length; i++) {
            if (seleccionadas[i]) {
                manoJugada[idx++] = mano[i];
            }
        }

        Object[] resultado = EvaluadorManos.evaluar(manoJugada);
        EvaluadorManos.TipoMano tipo = (EvaluadorManos.TipoMano) resultado[0];
        int puntosMano = (int) resultado[1];

        puntuacionAcumulada += puntosMano;
        manosRestantes--;

        for (int i = 0; i < mano.length; i++) {
            if (seleccionadas[i]) {
                mano[i] = baraja.robarCarta();
                seleccionadas[i] = false;
            }
        }
        ordenarMano();

        mensajeEstado = "Jugada: " + tipo + " (+" + puntosMano + " pts) | Total ronda: " + puntuacionAcumulada
                + " | Total acumulado: " + puntuacionTotal;

        if (puntuacionAcumulada >= OBJETIVOS[nivelActual-1]) {
            puntuacionTotal += puntuacionAcumulada;
            if (nivelActual == 3) {
                mensajeEstado = "¡VICTORIA! Puntuación final: " + puntuacionTotal;
                juegoTerminado = true;
                pcs.firePropertyChange("juegoTerminado", false, true);
                dbManager.insertarPartida(nombreJugador, puntuacionTotal, nivelActual);
            } else {
                nivelActual++;
                puntuacionAcumulada = 0;
                mensajeEstado = "¡Nivel superado! Pasas al nivel " + nivelActual
                        + " | Total acumulado: " + puntuacionTotal;
                iniciarNivel();
                return;
            }
        } else if (manosRestantes == 0) {
            mensajeEstado = "GAME OVER. Sin manos. Puntuación total: " + puntuacionTotal;
            juegoTerminado = true;
            pcs.firePropertyChange("juegoTerminado", false, true);
            dbManager.insertarPartida(nombreJugador, puntuacionTotal, nivelActual);
        }

        repaint();
    }

    /**
     * Descarta las cartas seleccionadas (entre 1 y 5) y las reemplaza por nuevas del mazo.
     * <p>
     * Si no hay ninguna carta seleccionada, muestra un mensaje de aviso.
     * Si no quedan descartes disponibles, no hace nada.
     */
    public void descartar() {
        if (juegoTerminado || descartesRestantes <= 0) return;

        int count = 0;
        for (boolean b : seleccionadas) if (b) count++;
        if (count == 0) {
            mensajeEstado = "Selecciona al menos una carta para descartar.";
            repaint();
            return;
        }

        for (int i = 0; i < mano.length; i++) {
            if (seleccionadas[i]) {
                mano[i] = baraja.robarCarta();
                seleccionadas[i] = false;
            }
        }
        descartesRestantes--;
        ordenarMano();

        mensajeEstado = "Nivel " + nivelActual + " | Objetivo: " + OBJETIVOS[nivelActual-1]
                + " pts | Manos: " + manosRestantes + " | Descartas: " + descartesRestantes
                + " | Total acumulado: " + puntuacionTotal;
        repaint();
    }

    /**
     * Ordena la mano y el array de selecciones para que se muestren como en Balatro:
     * As primero, luego K, Q, J, 10, 9...2. Los palos no influyen en el orden.
     */
    private void ordenarMano() {
        Integer[] indices = new Integer[mano.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                int v1 = mano[i1].getValor();
                int v2 = mano[i2].getValor();
                if (v1 == 14 && v2 != 14) return -1;
                if (v2 == 14 && v1 != 14) return 1;
                if (v1 == 14 && v2 == 14) return 0;
                return Integer.compare(v2, v1);
            }
        });

        Carta[] manoOrdenada = new Carta[mano.length];
        boolean[] selecOrdenada = new boolean[seleccionadas.length];
        for (int i = 0; i < indices.length; i++) {
            int idx = indices[i];
            manoOrdenada[i] = mano[idx];
            selecOrdenada[i] = seleccionadas[idx];
        }
        mano = manoOrdenada;
        seleccionadas = selecOrdenada;
    }

    /**
     * Método sobrescrito de {@link JPanel} que se llama automáticamente cada vez que se
     * necesita repintar el componente (por ejemplo, tras llamar a {@link #repaint()}).
     * <p>
     * Dibuja:
     * <ul>
     *   <li>El mensaje de estado en la parte superior.</li>
     *   <li>Las 8 cartas como rectángulos redondeados con el valor y palo.</li>
     *   <li>Una tabla de puntuaciones a la derecha.</li>
     *   <li>Un mensaje de "Fin del juego" si la partida terminó.</li>
     * </ul>
     *
     * @param g Contexto gráfico protegido por Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString(mensajeEstado, 20, 30);

        int cardsTotalWidth = 8 * CARD_WIDTH + 7 * MARGIN;
        int startX = 50;
        int y = 150;

        for (int i = 0; i < 8; i++) {
            int x = startX + i * (CARD_WIDTH + MARGIN);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);

            if (seleccionadas[i]) {
                g2.setColor(new Color(0, 255, 255, 60));
                g2.fillRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);
            }

            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(x, y, CARD_WIDTH, CARD_HEIGHT, 10, 10);

            if (mano[i] != null) {
                int palo = mano[i].getPalo();
                if (palo == 0 || palo == 1) {
                    g2.setColor(Color.RED);
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.drawString(mano[i].toString(), x + 8, y + 50);
            }
        }

        int tableX = startX + cardsTotalWidth + 40;
        int tableY = y;
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("TABLA DE PUNTUACIONES", tableX, tableY - 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        String[][] tabla = {
                {"Escalera Real", "1000"},
                {"Escalera Color", "750 + alta"},
                {"Póker", "500 + valor"},
                {"Full", "350 + valor"},
                {"Color", "300 + alta"},
                {"Escalera", "250 + alta"},
                {"Trío", "200 + valor"},
                {"Doble Pareja", "150 + mayor"},
                {"Pareja", "100 + valor"},
                {"Carta Alta", "valor más alto"}
        };
        int row = 0;
        for (String[] fila : tabla) {
            g2.drawString(fila[0], tableX, tableY + row * 18);
            g2.drawString(fila[1], tableX + 120, tableY + row * 18);
            row++;
        }

        if (juegoTerminado) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString("Fin del juego", 400, 400);
        }
    }

    /**
     * Detecta clics del ratón sobre las cartas para alternar su selección,
     * respetando el límite máximo de {@value MAX_SELECCION} cartas seleccionadas.
     * <p>
     * Si ya hay {@value MAX_SELECCION} cartas seleccionadas y se intenta seleccionar otra,
     * no se permite y se muestra un mensaje breve.
     *
     * @param e Evento de ratón con las coordenadas del clic
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (juegoTerminado) return;

        int mouseX = e.getX();
        int mouseY = e.getY();

        int cardsTotalWidth = 8 * CARD_WIDTH + 7 * MARGIN;
        int startX = 50;
        int y = 150;

        for (int i = 0; i < 8; i++) {
            int x = startX + i * (CARD_WIDTH + MARGIN);
            if (mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
                if (seleccionadas[i]) {
                    seleccionadas[i] = false;
                    repaint();
                    break;
                }
                int selecCount = 0;
                for (boolean b : seleccionadas) if (b) selecCount++;
                if (selecCount >= MAX_SELECCION) {
                    mensajeEstado = "Máximo " + MAX_SELECCION + " cartas seleccionables.";
                    repaint();
                    break;
                }
                seleccionadas[i] = true;
                repaint();
                break;
            }
        }
    }

    /** Ignorado */
    @Override public void mousePressed(MouseEvent e) {}
    /** Ignorado */
    @Override public void mouseReleased(MouseEvent e) {}
    /** Ignorado */
    @Override public void mouseEntered(MouseEvent e) {}
    /** Ignorado */
    @Override public void mouseExited(MouseEvent e) {}
}