import java.util.ArrayList;
import java.util.Collections;

/**
 * Modela una baraja francesa de 52 cartas.
 * Permite robar cartas y reiniciar la baraja cuando se agota.
 */
public class Baraja {
    /** Lista que contiene las cartas disponibles */
    private ArrayList<Carta> cartas;

    /**
     * Construye la baraja, generando las 52 cartas y barajándolas.
     */
    public Baraja() {
        cartas = new ArrayList<>();
        for (int palo = 0; palo <= 3; palo++) {
            for (int valor = 2; valor <= 14; valor++) {
                cartas.add(new Carta(valor, palo));
            }
        }
        Collections.shuffle(cartas);
    }

    /**
     * Roba la primera carta de la baraja. Si no quedan cartas, reinicia la baraja automáticamente.
     * @return Carta robada
     */
    public Carta robarCarta() {
        if (cartas.isEmpty()) {
            reiniciar();
        }
        return cartas.remove(0);
    }

    /**
     * Reinicia la baraja con las 52 cartas y las vuelve a barajar.
     */
    public void reiniciar() {
        cartas.clear();
        for (int palo = 0; palo <= 3; palo++) {
            for (int valor = 2; valor <= 14; valor++) {
                cartas.add(new Carta(valor, palo));
            }
        }
        Collections.shuffle(cartas);
    }

    /**
     * @return Número de cartas que quedan en la baraja
     */
    public int cartasRestantes() {
        return cartas.size();
    }
}