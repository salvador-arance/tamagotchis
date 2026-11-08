# Ejercicio PSP: Tamagotchis

## Requerimientos

- El programa principal es el "cuidador" de X Tamagotchis. Les pone las pilas y los lanza al mundo.
- Identificados y diferenciados entre ellos. Están vivos a la vez que el cuidador. No hay relación directa entre ellos, se relacionan a través del cuidador. Un tamagotchi solo puede realizar una acción al mismo tiempo.
- Se alimentan. Cada uno a diferente ritmo. Cada uno puede comerse una manzana a diferentes ritmos. Avisan cuando empiezan y cuando terminan de comer.
- Juegan: plantean al cuidador una suma entre dos números de una cifra cuyo resultado es menor que 10. el cuidador debe responder. Si el resultado es incorrecto, El tamagotchi sigue jugando.
- Se limpian. Se van ensuciando a medida que pasa el tiempo. Suciedad en una escala del 0 al 10. Cada 20 segundos se incrementa una unidad y al limpiarse vuelve a 0. El baño son 5 segundos. Cuando llega a 5 el tamagotchi avisa de que está sucio. Si llega a 10 el tamagotchi se muere.
- Tamagotchis tienen un tiempo de vida de 5 minutos. cuando ese tiempo pasa se mueren.
- El cuidador en todo momento podrá lanzar acciones a los tamagotchis y puede saber el estado de sus tamagotchis. también los puede matar, pero solo se mueren si están ociosos.

## Modelo de concurrencia

- Hay un hilo por cuidador y un hilo por tamagotchi.
- El hilo del tamagotchi es un bucle que cada -100ms comprueba el tiempo. (vida, suciedad, fin de la acción en curso).
- El cuidador llama directamente a los métodos del tamagotchi.
- Quien avanza el tiempo y termina las acciones es el bucle del tamagotchi.

## Análisis de clases necesarias

### Main

#### Atributos

- Cuidador

#### Flujo de MAIN

- Cuidador Nace
- Usuario elige el nombre del cuidador
- El cuidador se inicializa y el usuario crea e interactua con los tamagotchis a través de él.
- Se mueren todos los tamagotchis -> el cuidador puede elegir volver a inicializar los tamagotchis que quiera y seguir jugando o para el programa. El programa puede empezar desde el principio.

### Cuidador [[RUNNEABLE]]

#### Atributos

- Nombre -> String
- Tamagotchis a su cargo -> CopyOnWriteArrayList

#### Métodos

- Constructor
	- Asignación de nombre. Inicialización de su lista de tamagotchis.
- Inicializar un tamagotchi
	- Asignación de nombre, id y agregado en lista.
- Interacciones con los tamagotchis:
	- Recibir juego de un tamagotchi
		Contestar con suma.
		El tamagotchi seguirá preguntando hasta que la respuesta sea correcta.
	- Limpiarlo
		Intenta lanzar el método limpiar de un tamagotchi concreto.
	- Darle de comer
		- Lanza el método el método alimentarse de un tamagotchi concreto.
	- Matar tamagotchi
		- Intenta lanzar el método morir de un tamagotchi concreto.
	- Preguntar por estado
		- Lanza el método revelar estado de un tamagotchi.
		- Si el estado de alguno de los tamagotchis ha muerto, sacarlo de la lista.
- 
- RUN

#### Flujo cuidador

- Menú con opciones:
	- Inicializar Tamagotchi
	- Dejar de jugar
	(si hay 1 o más tamagotchis):
		- Limpiar a alguno de los tamagotchis
		- Darle de comer a alguno de los tamagotchis
		- Matar un tamagotchi
		- Comprobar estado de los tamagotchis
		
Jugar NO sería una opción del menú ya que la proposición de jugar nace de los tamagotchis. 
	

### Tamagotchis [[RUNNEABLE]]

#### Constantes para todos los tamagotchis

- Límite de suciedad antes de morir (10)
- Tiempo de vida (5 minutos)
- Límite de suciedad antes de avisar (5)
- Tiempo que tarda en ensuciarse una unidad (20 segundos)
- Tiempo que tarda en lavarse (5 segundos)

#### Atributos

- Nombre -> String
- Nivel de suciedad -> int
- Está vivo? -> boolean
- Está ocupado? -> AtomicBoolean
- Ritmo interno -> int
- rnd para génerar ritmo interno y proponer jugar al cuidador

#### Métodos

- Nacer
	- Se inicializan sus atributos en el constructor y se establece el ritmo interno aleatorio.
- Establecer ritmo interno aleatorio.
	- Se genera un número de ms concreto que se almacenará en ritmo interno. Esto será lo que tarde en comer.
- Ensuciarse
	- Empieza el temporizador. Cada vez que pasa la cantidad determinada, se aumenta el nivel de suciedad
- Alimentarse
	- Avisar de que ha empezado a comer.
	- Esperar un rato.
	- Avisar de que se ha terminado de comer.
- Jugar
	- Cambia su estado a ocupado.
	- Lanzar pregunta de juego.
	- Esperar respuesta del jugador.
	- Determinar si la respuesta es correcta.
	- Respuesta incorrecta -> Repetir.
	- Respuesta correcta -> Cambiar estado a libre y parar.
- Limpiarse:
	- Esperar el tiempo determinado en la constante, restablecer suciedad a 0.
- Morirse:
	- Informar de que el tamagotchi a muerto.
	- Cambiar estado a muerto.
- Revelar estado:
	- Imprimir en pantalla:
		- Id
		- Nombre
		- Tiempo que queda para que se muera.
		- Nivel de suciedad.
		- Si está ocupado.
- Gestionar interacción.
	- Recibe la acción del cuidador, si está ocupado la rechaza.
	- Si está libre, cambia su estado a ocupado.
	- Espera a que la acción termine y cambia el estado de ocupado a libre.
- Proponer limpieza
	- Imprimir en pantalla aviso de limpieza.
- RUN

#### Flujo tamagotchi

- Nace.
- Empieza el temporizador que cuando termina hace que el tamagotchi muera.
- Empieza a ensuciarse.
- Si el temporizador de muerte llega al tiempo declarado, se lanza el método morir.
- Si la suciedad llega a nivel de aviso, lanzar método proponer limpieza.
- Si la suciedad llega a nivel de muerte, lanzar método morir.
- Bucle principal:
	- Si no está ocupado, de vez en cuando propone jugar al jugador.
	- Espera acciones del cuidador.
		- Si la acción que le llega es alimentarse:
			- Si está ocupado, se lo comunica al cuidador.
			- Si está libre, cambia su estado a ocupado y lanza la acción alimentarse.
		- Si la acción que le llega es limpiarse:
			-Si está ocupado rechaza, si no, cambia su estado a ocupado y se limpia.
		- Si la acción que le llega es una consulta sobre su estado, siempre revela su estado y no cambia su estado a ocupado.
		- Si la acción que le llega es morirse, si no está ocupado muere.
	
		
		