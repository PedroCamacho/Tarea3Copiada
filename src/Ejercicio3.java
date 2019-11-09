import java.util.HashMap;

public class Ejercicio3 {
	public static void main(String[] args) {
		HashMap<Integer, Proceso> listaProcesos = new HashMap<Integer, Proceso>();
		HD discoDuro = new HD(listaProcesos);
		Needle needle = new Needle(discoDuro);
		for (int i = 1; i <= 10; i++) {
			listaProcesos.put(i, new Proceso(i,discoDuro));
			listaProcesos.get(i).start();
		}
		needle.start();
		try {
			for (int i = 1; i <= 10; i++) {
				listaProcesos.get(i).join();
			}
			listaProcesos.clear();
			discoDuro.notifica();
			needle.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("FIN DE EJECUCION");
	}
}

class HD {
	private boolean buffering = false;
	private int pistaActual = 0;
	private boolean mainTermina = false;
	private HashMap<Integer, Proceso> listaProcesos;

	public HD(HashMap<Integer, Proceso> listaProcesos) {
		this.listaProcesos = listaProcesos;
	}
	
	public synchronized void notifica() {
		mainTermina = true;
		notify();
	}

	public synchronized void leerFichero(int pistaLeyendo) {
		try {
			while (buffering && this.pistaActual != pistaLeyendo) {
				wait();
			}
			if (pistaActual != pistaLeyendo) {

				buffering = true;
				pistaActual = pistaLeyendo;
				System.out.println("Reading " + pistaLeyendo);
				notifyAll();
			} else {
				System.out.println("Sharing " + pistaLeyendo);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void ficheroLeido() {

		while (!listaProcesos.isEmpty()) {
			try {
				while (!buffering && !mainTermina) {
					wait();
				}
				buffering = false;
				Thread.currentThread().sleep((int) (Math.random() * 1000 + 1000));
				System.out.println("Read " + pistaActual);
				notifyAll();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class Proceso extends Thread {
	private int numPista;
	private HD discoDuro;
	private int[] pistas = new int[10];

	public Proceso(Integer posicion, HD discoDuro) {
		this.numPista = posicion;
		this.discoDuro = discoDuro;
		ponerFichero();
	}

	public void run() {
		for (int i = 0; i < 10; i++) {
			try {
				sleep((int) (Math.random() * 1000 + 1000));
				discoDuro.leerFichero(pistas[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Termina el proceso: " + numPista);
	}
	
	public void ponerFichero() {
		for (int i = 0; i < 10; i++) {
			pistas[i] = (int) (Math.random() * 20 + 1);
		}
	}
}

class Needle extends Thread {
	private HD discoDuro;

	public Needle(HD discoDuro) {
		this.discoDuro = discoDuro;
	}

	public void run() {
		discoDuro.ficheroLeido();
	}
}
