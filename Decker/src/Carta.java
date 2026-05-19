/**
 * Representa una carta de la baraja francesa con un valor numérico (2-14) y un palo (0-3).
 * El valor 11 es J, 12 Q, 13 K y 14 As.
 * El palo 0 = Corazones, 1 = Diamantes, 2 = Tréboles, 3 = Picas.
 */
public class Carta {
    /** Valor de la carta (2..14) */
    private int valor;
    /** Palo de la carta (0=♥, 1=♦, 2=♣, 3=♠) */
    private int palo;

    /**
     * Construye una carta con el valor y palo indicados.
     * @param valor Valor numérico entre 2 y 14
     * @param palo  Índice del palo (0-3)
     */
    public Carta(int valor, int palo) {
        this.valor = valor;
        this.palo = palo;
    }

    /**
     * @return Valor numérico de la carta
     */
    public int getValor() {
        return valor;
    }

    /**
     * @return Índice del palo (0-3)
     */
    public int getPalo() {
        return palo;
    }

    /**
     * Representación textual de la carta, por ejemplo "A♥", "10♠".
     * @return Cadena con el valor y el símbolo del palo
     */
    @Override
    public String toString() {
        String[] nombresValor = {"", "", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] nombresPalo = {"♥", "♦", "♣", "♠"};
        return nombresValor[valor] + nombresPalo[palo];
    }

    /**
     * Compara si dos cartas son iguales (mismo valor y mismo palo).
     * @param obj Objeto a comparar
     * @return true si son la misma carta, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Carta) {
            Carta otra = (Carta) obj;
            return this.valor == otra.valor && this.palo == otra.palo;
        }
        return false;
    }
}