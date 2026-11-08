package org.cuatrovientos.dam.psp.Tamagotchis;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
	
	public enum ACCIONES {
		JUGANDO,
		COMIENDO,
		LIMPIANDOSE,
		OCIOSO
	}
	
	private AtomicReference<ACCIONES> accionActual = new AtomicReference<ACCIONES>();
	private AtomicReference<ACCIONES> accionPendiente = new AtomicReference<ACCIONES>();
	private AtomicReference<Integer> respuestaCuidador = new AtomicReference<Integer>();
	
	public Tamagotchi(String nombre) {
		this.nombre = nombre;
		this.vivo = new AtomicBoolean(true);
		this.rnd = new Random();
		this.ritmoInterno = establecerRitmoInterno();
		this.accionActual.set(ACCIONES.OCIOSO);
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
	
	public boolean soyAsesinado() {
		if (this.accionActual.get() == ACCIONES.OCIOSO) {
			morir();
			return true;
		}
		return false;
	}
	
	public boolean alimentarse() {
		if (this.accionActual.compareAndSet(ACCIONES.OCIOSO, ACCIONES.COMIENDO)) {
			System.out.println("Estoy empezando a comer.");
			try  {
				Thread.sleep(ritmoInterno);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Acabo de terminar de comer.");
			this.accionActual.set(ACCIONES.OCIOSO);
			return true;
		}
		return false;
	}
	
	private void jugar() {
		if (this.accionActual.compareAndSet(ACCIONES.OCIOSO, ACCIONES.JUGANDO)) {
			int numero1 = rnd.nextInt(10);
			int numero2 = rnd.nextInt(10 - numero1);
			int resultado = numero1 + numero2;

			this.respuestaCuidador.set(null);

			boolean respuestaCorrecta = false;

			while (!respuestaCorrecta && this.vivo.get()) {
				System.out.println("Soy " + this.nombre + " y quiero jugar: ¿cuánto es "
						+ numero1 + " + " + numero2 + "?");

				Integer respuesta = null;
				while (respuesta == null && this.vivo.get()) {
					respuesta = this.respuestaCuidador.getAndSet(null);
					if (respuesta == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				if (respuesta != null) {
					respuestaCorrecta = respuesta == resultado;
					if (!respuestaCorrecta) {
						System.out.println("Soy " + this.nombre + ": respuesta incorrecta, sigo jugando.");
					}
				}
			}

			if (respuestaCorrecta) {
				System.out.println("Soy " + this.nombre + ": ¡has acertado!");
			}

			this.accionActual.set(ACCIONES.OCIOSO);
		}
	}
	
	public boolean limpiarse() {
		if (this.accionActual.compareAndSet(ACCIONES.OCIOSO, ACCIONES.LIMPIANDOSE)) {
			System.out.println("Estoy empezando a limpiarme.");
			try  {
				Thread.sleep(TIEMPO_LAVARSE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			this.nivelSuciedad.set(0);
			
			System.out.println("Acabo de terminar de LIMPIARME.");
			this.accionActual.set(ACCIONES.OCIOSO);
			return true;
		}
		return false;
	}
	
	public void revelarEstado() {
		System.out.println("Soy " + this.nombre +"\n"
				+ "Nivel de suciedad: " + this.nivelSuciedad + "\n"
						+ "Accion actual: " + this.accionActual);
	}
	
	private void proponerLimpieza() {
		System.out.println("Mi nivel de suciedad es insoportable, si no me limpias moriré!");
	}
	
	public boolean ordenar(ACCIONES accion) {
		if (accionPendiente.compareAndSet(null, accion)) {
			return true;
		}
		return false;
	}
	
	public void responder(int respuesta) {
	    this.respuestaCuidador.set(respuesta);
	}
	
	@Override
	public void run() {
		Thread ensuciarse = new Thread(() -> ensuciarse());
		Thread morirse = new Thread(() -> cuentaAtrasDeLaMuerte());
		ensuciarse.start();
		morirse.start();
		
		while (vivo.get()) {
		    ACCIONES orden = accionPendiente.getAndSet(null);

		    if (orden == null) {
		        try {
		        	if (rnd.nextInt(500) == 0) {
		        		jugar();
		        	}
		            Thread.sleep(100);
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		        continue;
		    }

		    switch (orden) {
		        case COMIENDO:
		            if (alimentarse()) {
		                System.out.println("Orden de comer cumplida.");
		            } else {
		                System.out.println("No he podido comer.");
		            }
		            break;
		        case LIMPIANDOSE:
		            if (limpiarse()) {
		                System.out.println("Orden de limpiarme cumplida.");
		            } else {
		                System.out.println("No he podido limpiarme.");
		            }
		            break;
		        default:
		            break;
		    }
		}
	}
	
	public boolean isVivo() {
		return this.vivo.get();
	}
	
	public String getNombre() {
		return nombre;
	}

	public int getNivelSuciedad() {
		return nivelSuciedad.get();
	}

	public int getRitmoInterno() {
		return ritmoInterno;
	}

	public Random getRnd() {
		return rnd;
	}
}
