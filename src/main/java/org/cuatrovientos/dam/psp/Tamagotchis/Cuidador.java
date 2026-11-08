package org.cuatrovientos.dam.psp.Tamagotchis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.cuatrovientos.dam.psp.Tamagotchis.Tamagotchi.ACCIONES;

public class Cuidador {
	// ATRIBUTOS
	private List<Tamagotchi> misTamagotchis;
	private List<Thread> misHilos;
	private Scanner sc = new Scanner(System.in);
	
	//CONSTRUCTOR
	
	public Cuidador(int cantidadTamagotchis) {
		for (int i = 1; i <= cantidadTamagotchis; i ++ ) {
			String nombreTamagotchi = "Tamagotchi " + i;
			Tamagotchi t = new Tamagotchi(nombreTamagotchi);
			misTamagotchis.add(t);
		}
	}
	
	// MÉTODOS
	
	public void lanzarTamagotchis() {
		for (Tamagotchi t: misTamagotchis) {
			Thread h = new Thread(t);
			h.start();
		}
	}
	
	public void alimentar(Tamagotchi t) {
		if (t.ordenar(ACCIONES.COMIENDO)) {
			System.out.println("Orden de comer enviada.");
			return;
		}
		
		System.out.println("Orden de comer rechazada. Está ocupado");
	}
	
	public void limpiar(Tamagotchi t) {
		if (t.ordenar(ACCIONES.COMIENDO)) {
			System.out.println("Orden de comer enviada.");
			return;
		}
		
		System.out.println("Orden de limpiar rechazada. Está ocupado.");
	}
	
	public void matarTamagotchi(Tamagotchi t) {
		if (t.soyAsesinado()) {
			System.out.println("Ha muerto");
			return;
		}
		
		System.out.println("no se puede matar: está ocioso.");
	}
	
	public void responder(Tamagotchi t, int numero) {
		t.responder(numero);
	}
	
	public void mostrarEstados() {
		for (Tamagotchi t: misTamagotchis) {
			t.revelarEstado();
			if (t.isVivo()) {
				System.out.println("vivo");
			} else {
				System.out.println("muerto");
			}
		}
	}
	
	public Tamagotchi elegirTamagotchi() {
		for (int i = 0; i < misTamagotchis.size(); i++) {
			System.out.println(i + misTamagotchis.get(i).getNombre() + " vivo: " + misTamagotchis.get(i).isVivo());
		}
		
		System.out.println("Elige uno de ellos introduciendo su número: ");
		
		int indiceTamagotchi = pedirEntero();
		
		return misTamagotchis.get(indiceTamagotchi);
	}
	
	public void menu() {
		while (misTamagotchis.size() > 0) {
			System.out.println("=====OPCIONES=====");
			System.out.println("1. ALIMENTAR");
			System.out.println("2. LIMPIAR");
			System.out.println("3. MATAR");
			System.out.println("4. Responder una suma");
			System.out.println("5. Ver estados");
			System.out.println("0. Salir");
			
			boolean opcionValida = false;
			int opcion = 0;
			
			while (!opcionValida) {
				opcion = pedirEntero();
				
				if (opcion < 0 || opcion > 5) {
					System.out.println("OPCIÓN NO VÁLIDA!");
				}
			}
			
			switch (opcion){
			case 0:
				return;
			case 1:
				alimentar(elegirTamagotchi());
				break;
			case 2:
				limpiar(elegirTamagotchi());
				break;
			case 3:
				matarTamagotchi(elegirTamagotchi());
				break;
			case 4:
				Tamagotchi t = elegirTamagotchi();
				int respuesta = pedirEntero();
				responder(t, respuesta);
				break;
			case 5:
				mostrarEstados();
				break;
			}
			
			
		}
	}
	
	private int pedirEntero() {
		String entrada = "";
		int numero = 0;
		boolean formatoCorrecto = false;
		
		while (!formatoCorrecto) {
			System.out.print("Introduce el número: ");
			try {
				entrada = sc.nextLine();
				numero = Integer.parseInt(entrada);
				formatoCorrecto = true;
			}catch (NumberFormatException e) {
				System.out.println("ERROR: El formato no es correcto.");
			}
		}
		
		return numero;
	}
	
}
