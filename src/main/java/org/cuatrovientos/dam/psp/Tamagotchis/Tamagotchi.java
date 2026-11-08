package org.cuatrovientos.dam.psp.Tamagotchis;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Tamagotchi implements Runnable {
	
	private final static int LIMITE_SUCIEDAD_ANTES_DE_MORIR = 10;
	private final static int LIMITE_SUCIEDAD_ANTES_DE_AVISAR = 5;
	private final static int TIEMPO_DE_VIDA = 5 * 60 * 1000;
	private final static int TIEMPO_ENSUCIARSE = 20 * 1000;
	private final static int TIEMPO_LAVARSE = 5 * 1000;
	
	private String nombre;

	private AtomicInteger nivelSuciedad = new AtomicInteger(0);
	private AtomicBoolean vivo ;
	private int ritmoInterno = 0;
	private Random rnd;
	private Scanner sc = new Scanner(System.in);
	
	public enum ACCIONES {
		JUGANDO,
		COMIENDO,
		LIMPIANDOSE,
		OCIOSO
	}
	
	private volatile ACCIONES accionActual = ACCIONES.OCIOSO;
	
	public Tamagotchi(String nombre) {
		this.nombre = nombre;
		this.vivo = new AtomicBoolean(true);
		this.rnd = new Random();
		this.ritmoInterno = establecerRitmoInterno();
	}
	
	private int establecerRitmoInterno() {
		return rnd.nextInt(100, 1000);
	}
	
	private void morir() {
		if (this.vivo.compareAndSet(true, false)) {
			System.out.print("Soy " + this.nombre + " y me voy a morir.");
		}
	}
	
	private void ensuciarse() {
		while (this.vivo.get()) {
			try {
				Thread.sleep(TIEMPO_ENSUCIARSE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int nuevoValorSuciedad = this.nivelSuciedad.incrementAndGet();
			
			if (nuevoValorSuciedad == LIMITE_SUCIEDAD_ANTES_DE_AVISAR) {
				this.proponerLimpieza();
			}
			
			if (nuevoValorSuciedad  == LIMITE_SUCIEDAD_ANTES_DE_MORIR) {
				this.morir();
			}
		}
	}
	
	private void cuentaAtrasDeLaMuerte() {
		while (this.vivo.get()) {
			try {
				Thread.sleep(TIEMPO_DE_VIDA);
				if (this.vivo.get()) {
					this.morir();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void alimentarse() {
		this.accionActual = ACCIONES.COMIENDO;
		System.out.println("Estoy empezando a comer.");
		try  {
			Thread.sleep(ritmoInterno);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Acabo de terminar de comer.");
		this.accionActual = ACCIONES.OCIOSO;
	}
	
	private synchronized void jugar() {
		this.accionActual = ACCIONES.JUGANDO;
		
		boolean respuestaCorrecta = false;
		int numero1 = 0;
		int numero2 = 0;
		
		int resultado = 0;
		
		while (!respuestaCorrecta) {
			System.out.println("EMPIEZA EL JUEGO: Dime dos números que, sumándolos, su resultado sea menor que 10.");
			numero1 = pedirInt();
			numero2 = pedirInt();
			resultado = numero1 + numero2;
			respuestaCorrecta = resultado < 10;
			if (!respuestaCorrecta) {
				System.out.println("Te has equivocado!");
			}
		}
		
		System.out.println("Has ganado!");
		
		this.accionActual = ACCIONES.OCIOSO;
	}
	
	private synchronized void limpiarse() {
		this.accionActual = ACCIONES.LIMPIANDOSE;
		System.out.println("Estoy empezando a limpiarme.");
		try  {
			Thread.sleep(TIEMPO_LAVARSE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.nivelSuciedad.set(0);
		
		System.out.println("Acabo de terminar de LIMPIARME.");
		this.accionActual = ACCIONES.OCIOSO;
	}
	
	private void revelarEstado() {
		System.out.println("Soy " + this.nombre +"\n"
				+ "Nivel de suciedad: " + this.nivelSuciedad + "\n"
						+ "Accion actual: " + this.accionActual);
	}
	
	private void proponerLimpieza() {
		System.out.println("Mi nivel de suciedad es insoportable, si no me limpias moriré!");
	}
	
	@Override
	public void run() {
		Thread ensuciarse = new Thread(() -> ensuciarse());
		Thread morirse = new Thread(() -> cuentaAtrasDeLaMuerte());
		ensuciarse.start();
		morirse.start();
	}
	
	public String getNombre() {
		return nombre;
	}

	private void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getNivelSuciedad() {
		return nivelSuciedad.get();
	}

	private void setNivelSuciedad(int nivelSuciedad) {
		this.nivelSuciedad.set(nivelSuciedad);
	}

	public int getRitmoInterno() {
		return ritmoInterno;
	}

	private void setRitmoInterno(int ritmoInterno) {
		this.ritmoInterno = ritmoInterno;
	}

	public Random getRnd() {
		return rnd;
	}

	private void setRnd(Random rnd) {
		this.rnd = rnd;
	}
	
	private int pedirInt() {
		boolean formatoCorrecto = false;
		int numero = 0;
		
		while (!formatoCorrecto) {
			System.out.print("Introduce un número: ");
			String entrada = "";
			try {
				entrada = sc.nextLine();
				numero = Integer.parseInt(entrada);
				formatoCorrecto = true;
			} catch (NumberFormatException e) {
				System.out.println("Error: deberías introducir número.");
			}
		}
		return numero;
	}

}
