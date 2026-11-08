# Ejercicio PSP: Tamagotchis

## Requerimientos

- El programa principal es el "cuidador" de X Tamagotchis. Les pone las pilas y los lanza al mundo.
- Identificados y diferenciados entre ellos. Están vivos a la vez que el cuidador. No hay relación directa entre ellos, se relacionan a través del cuidador. Un tamagotchi solo puede realizar una acción al mismo tiempo.
- Se alimentan. Cada uno a diferente ritmo. Cada uno puede comerse una manzana a diferentes ritmos. Avisan cuando empiezan y cuando terminan de comer.
- Juegan: plantean al cuidador una suma entre dos números de una cifra cuyo resultado es menor que 10. El cuidador debe responder. Si el resultado es incorrecto, el tamagotchi sigue jugando.
- Se limpian. Se van ensuciando a medida que pasa el tiempo. Suciedad en una escala del 0 al 10. Cada 20 segundos se incrementa una unidad y al limpiarse vuelve a 0. El baño son 5 segundos. Cuando llega a 5 el tamagotchi avisa de que está sucio. Si llega a 10 el tamagotchi se muere.
- Tamagotchis tienen un tiempo de vida de 5 minutos. Cuando ese tiempo pasa se mueren.
- El cuidador en todo momento podrá lanzar acciones a los tamagotchis y puede saber el estado de sus tamagotchis. También los puede matar, pero solo se mueren si están ociosos.

## Decisiones de diseño

- **Hilos:** uno por cuidador y uno por tamagotchi. Sin hilos auxiliares.
- **Tiempo:** cada tamagotchi tiene un único bucle que comprueba el tiempo (vida y suciedad) y duerme un rato corto (~100 ms) entre vueltas.
- **Comunicación:** el cuidador llama directamente a métodos `synchronized` del tamagotchi. Esos métodos solo comprueban si está libre y dejan anotada la acción pedida. La acción la ejecuta el hilo del propio tamagotchi, así el menú del cuidador nunca se queda bloqueado.
- **Estado ocupado:** `AtomicBoolean`.
- **Lista de tamagotchis:** `Collections.synchronizedList`. Se recorre dentro de un bloque `synchronized`.
- **Juego:** el cuidador responde de forma automática (calcula la suma) y falla de vez en cuando de forma aleatoria, para que el "repetir hasta acertar" tenga sentido. Así no hay dos hilos leyendo de la consola a la vez.
- **Ritmo de comer:** se fija una sola vez al nacer.

## Análisis de clases necesarias

### Main

#### Atributos

- Cuidador

#### Flujo de MAIN

- Cuidador nace.
- Usuario elige el nombre del cuidador.
- El cuidador se inicializa y el usuario crea e interactúa con los tamagotchis a través de él.
- Se mueren todos los tamagotchis -> el cuidador puede elegir volver a inicializar los tamagotchis que quiera y seguir jugando, o parar el programa.

### Cuidador [[RUNNEABLE]]

#### Atributos

- Nombre -> String
- Tamagotchis a su cargo -> Lista sincronizada

#### Métodos

- Constructor
	- Asignación de nombre. Inicialización de su lista de tamagotchis.
- Inicializar un tamagotchi
	- Asignación de nombre, id y agregado en lista. Lanza su hilo.
- Recibir juego de un tamagotchi (`synchronized`)
	- Recibe los dos números, calcula la suma y la devuelve (de vez en cuando se equivoca a propósito).
	- El tamagotchi seguirá preguntando hasta que la respuesta sea correcta.
- Limpiar
	- Llama al método limpiarse de un tamagotchi concreto.
- Dar de comer
	- Llama al método alimentarse de un tamagotchi concreto.
- Matar
	- Llama al método morir de un tamagotchi concreto.
- Preguntar por estado
	- Llama al método revelarEstado de cada tamagotchi.
- Retirar muertos
	- Quita de la lista los tamagotchis que ya no estén vivos. Se hace antes de mostrar el menú.
- RUN

#### Flujo cuidador

- Bucle:
	- Retirar muertos de la lista.
	- Si la lista está vacía y ya se había creado alguno: preguntar si empezar de nuevo o salir.
	- Mostrar menú:
		- Inicializar Tamagotchi
		- Dejar de jugar
		(si hay 1 o más tamagotchis):
			- Limpiar a alguno de los tamagotchis
			- Darle de comer a alguno de los tamagotchis
			- Matar un tamagotchi
			- Comprobar estado de los tamagotchis
	- Ejecutar la opción elegida.

Jugar NO es una opción del menú, ya que la proposición de jugar nace de los tamagotchis.

### Tamagotchis [[RUNNEABLE]]

#### Constantes para todos los tamagotchis

- Límite de suciedad antes de morir (10)
- Tiempo de vida (5 minutos)
- Límite de suciedad antes de avisar (5)
- Tiempo que tarda en ensuciarse una unidad (20 segundos)
- Tiempo que tarda en lavarse (5 segundos)

#### Atributos

- Nombre -> String
- Id -> `static AtomicInteger` (compartido para que cada id sea único)
- Nivel de suciedad -> int
- Está vivo? -> boolean (`volatile`)
- Está ocupado? -> `AtomicBoolean`
- Acción pedida por el cuidador -> enum/String (NINGUNA, COMER, LIMPIAR, MORIR)
- Ritmo interno -> int (ms que tarda en comer)
- Instante de nacimiento -> long
- Instante de la última suciedad -> long
- Aviso de suciedad ya dado -> boolean
- rnd -> Random (para el ritmo interno y para decidir cuándo proponer jugar)
- Referencia al cuidador

#### Métodos

- Nacer
	- Se inicializan sus atributos en el constructor, se guarda el instante de nacimiento y se fija el ritmo interno aleatorio.
- Establecer ritmo interno aleatorio
	- Se genera un número de ms que se guarda en ritmo interno. Es lo que tarda en comer. Se hace una sola vez.
- Peticiones del cuidador (`synchronized`, no bloquean)
	- `alimentarse()`, `limpiarse()`, `morir()`:
		- Si está ocupado, rechaza y lo comunica al cuidador.
		- Si está libre, se pone ocupado y anota la acción pedida. Devuelve "aceptada".
	- `revelarEstado()`: siempre responde, no cambia el estado de ocupado.
- Ejecutar alimentarse (lo llama el bucle del propio tamagotchi)
	- Avisar de que ha empezado a comer.
	- `sleep(ritmo interno)`.
	- Avisar de que ha terminado de comer.
	- Pasa a libre.
- Ejecutar limpiarse
	- `sleep(5 s)`, suciedad = 0, se reinicia el instante de la última suciedad y el aviso.
	- Pasa a libre.
- Jugar
	- Pasa a ocupado.
	- Genera dos números de una cifra cuya suma sea menor que 10.
	- Pregunta al cuidador. Si la respuesta es incorrecta, repite con la misma suma hasta acertar.
	- Pasa a libre.
- Morirse
	- Informar de que el tamagotchi ha muerto.
	- Cambiar estado a muerto.
- Revelar estado
	- Imprimir en pantalla: Id, Nombre, tiempo que queda para que se muera, nivel de suciedad y si está ocupado.
- Proponer limpieza
	- Imprimir en pantalla aviso de limpieza.
- RUN

#### Flujo tamagotchi

- Nace.
- Bucle principal, mientras esté vivo:
	1. Si ha pasado el tiempo de vida -> morir.
	2. Si han pasado 20 s desde la última suciedad -> suciedad +1 y se actualiza el instante.
	3. Si la suciedad llega a 5 y no ha avisado -> proponer limpieza (una sola vez).
	4. Si la suciedad llega a 10 -> morir (aunque esté ocupado).
	5. Si hay una acción pedida por el cuidador:
		- COMER -> ejecutar alimentarse.
		- LIMPIAR -> ejecutar limpiarse.
		- MORIR -> morir (solo llega aquí si estaba libre).
	6. Si está libre y no hay acción pedida, de vez en cuando (aleatorio) propone jugar al cuidador.
	7. `sleep(~100 ms)`.

Notas:
- La muerte por vida o por suciedad ocurre siempre. La muerte por orden del cuidador solo si está ocioso.
- Mientras el tamagotchi hace una acción (comer, limpiarse) su hilo está dormido, así que las comprobaciones de tiempo se retrasan unos segundos. El tiempo se calcula con instantes reales, por lo que al despertar se ponen al día. Para este ejercicio es aceptable.
		