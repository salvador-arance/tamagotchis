package org.cuatrovientos.dam.psp.Tamagotchis;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Cuidador cuidador = new Cuidador(5);
        cuidador.lanzarTamagotchis();
        cuidador.menu();
    }
}
