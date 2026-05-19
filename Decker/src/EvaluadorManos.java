import java.util.*;

/**
 * Clase estática que evalúa manos de póker.
 * Permite evaluar exactamente 5 cartas y también encontrar la mejor combinación
 * de 5 cartas dentro de una mano de 8.
 */
public class EvaluadorManos {

    /**
     * Tipos de mano de póker ordenados de menor a mayor valor.
     */
    public enum TipoMano {
        CARTA_ALTA, PAREJA, DOBLE_PAREJA, TRIO, ESCALERA, COLOR,
        FULL, POKER, ESCALERA_COLOR, ESCALERA_REAL
    }

    /**
     * Evalúa una mano de exactamente 5 cartas y devuelve su tipo y puntuación.
     * @param mano Array de 5 cartas
     * @return Object[] donde [0] es TipoMano y [1] es Integer con la puntuación
     */
    public static Object[] evaluar(Carta[] mano) {
        Arrays.sort(mano, Comparator.comparingInt(Carta::getValor));

        boolean esColor = esColor(mano);
        boolean esEscalera = esEscalera(mano);

        if (esColor && esEscalera && mano[4].getValor() == 14) {
            return new Object[]{TipoMano.ESCALERA_REAL, 1000};
        }
        if (esColor && esEscalera) {
            return new Object[]{TipoMano.ESCALERA_COLOR, 750 + mano[4].getValor()};
        }

        int[] reps = contarRepeticiones(mano);
        int maxRep = reps[0];
        int valorMaxRep = reps[1];
        int segundaRep = reps[2];

        if (maxRep == 4) {
            return new Object[]{TipoMano.POKER, 500 + valorMaxRep};
        }
        if (maxRep == 3 && segundaRep == 2) {
            return new Object[]{TipoMano.FULL, 350 + valorMaxRep};
        }
        if (esColor) {
            return new Object[]{TipoMano.COLOR, 300 + mano[4].getValor()};
        }
        if (esEscalera) {
            return new Object[]{TipoMano.ESCALERA, 250 + mano[4].getValor()};
        }
        if (maxRep == 3) {
            return new Object[]{TipoMano.TRIO, 200 + valorMaxRep};
        }
        if (maxRep == 2 && segundaRep == 2) {
            int mayorPareja = Math.max(reps[1], reps[3]);
            return new Object[]{TipoMano.DOBLE_PAREJA, 150 + mayorPareja};
        }
        if (maxRep == 2) {
            return new Object[]{TipoMano.PAREJA, 100 + valorMaxRep};
        }
        return new Object[]{TipoMano.CARTA_ALTA, mano[4].getValor()};
    }

    /**
     * Encuentra la mejor combinación de 5 cartas entre 8.
     * @param mano8 Array de 8 cartas
     * @return Object[] con [0] TipoMano, [1] Integer puntuación, [2] Carta[] con las 5 cartas usadas
     */
    public static Object[] evaluarMejorMano(Carta[] mano8) {
        List<Carta[]> combinaciones = generarCombinaciones(mano8, 5);
        TipoMano mejorTipo = TipoMano.CARTA_ALTA;
        int mejorPunt = 0;
        Carta[] mejorMano = null;

        for (Carta[] comb : combinaciones) {
            Object[] resultado = evaluar(comb);
            int punt = (int) resultado[1];
            if (punt > mejorPunt) {
                mejorPunt = punt;
                mejorTipo = (TipoMano) resultado[0];
                mejorMano = comb;
            }
        }
        return new Object[]{mejorTipo, mejorPunt, mejorMano};
    }

    /**
     * Verifica si las 5 cartas son del mismo palo.
     */
    private static boolean esColor(Carta[] mano) {
        int paloBase = mano[0].getPalo();
        for (int i = 1; i < mano.length; i++) {
            if (mano[i].getPalo() != paloBase) return false;
        }
        return true;
    }

    /**
     * Verifica si las 5 cartas forman una escalera (secuencia consecutiva de valores).
     * Incluye la escalera especial A-2-3-4-5 donde el As actúa como 1.
     */
    private static boolean esEscalera(Carta[] mano) {
        if (mano[0].getValor() == 2 && mano[1].getValor() == 3 &&
                mano[2].getValor() == 4 && mano[3].getValor() == 5 &&
                mano[4].getValor() == 14) {
            return true;
        }
        for (int i = 1; i < mano.length; i++) {
            if (mano[i].getValor() != mano[i-1].getValor() + 1) return false;
        }
        return true;
    }

    /**
     * Cuenta las repeticiones de valores en la mano.
     * @return Array con [maxRep, valorMaxRep, segundaRep, valorSegundaRep]
     */
    private static int[] contarRepeticiones(Carta[] mano) {
        int[] frecuencias = new int[15];
        for (Carta c : mano) {
            frecuencias[c.getValor()]++;
        }
        int maxRep = 0, valorMaxRep = 0;
        int segundaRep = 0, valorSegundaRep = 0;
        for (int v = 14; v >= 2; v--) {
            if (frecuencias[v] > maxRep) {
                segundaRep = maxRep;
                valorSegundaRep = valorMaxRep;
                maxRep = frecuencias[v];
                valorMaxRep = v;
            } else if (frecuencias[v] > segundaRep) {
                segundaRep = frecuencias[v];
                valorSegundaRep = v;
            }
        }
        return new int[]{maxRep, valorMaxRep, segundaRep, valorSegundaRep};
    }

    /**
     * Genera todas las combinaciones posibles de 'k' cartas a partir de un array.
     */
    private static List<Carta[]> generarCombinaciones(Carta[] cartas, int k) {
        List<Carta[]> resultado = new ArrayList<>();
        combinar(cartas, new Carta[k], 0, 0, resultado);
        return resultado;
    }

    private static void combinar(Carta[] origen, Carta[] temp, int inicio, int indice, List<Carta[]> resultado) {
        if (indice == temp.length) {
            resultado.add(temp.clone());
            return;
        }
        for (int i = inicio; i <= origen.length - (temp.length - indice); i++) {
            temp[indice] = origen[i];
            combinar(origen, temp, i + 1, indice + 1, resultado);
        }
    }
}