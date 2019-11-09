import java.util.HashMap;

public class Ejercicio3 {
	public static void main(String[] args) {
		HashMap<Integer, Proceso> listaProcesos = new HashMap<>();
		HD hardDisk = new HD(listaProcesos);
		for (int i = 0; i < 10; i++) {
			listaProcesos.put(i + 1, new Proceso(i + 1, hardDisk));
			listaProcesos.get(i + 1).start();
		}
		Needle needle = new Needle(hardDisk);
		needle.start();
		try {
			for (int i = 0; i < 10; i++) {
				listaProcesos.get(i + 1).join();
				listaProcesos.remove(i + 1);
			}
			hardDisk.setHasFinalized(true);
			needle.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("FIN DE EJECUCION");
	}
}

class HD {
	private boolean isReading = false;
	private boolean hasFinalized = false;
	private int currentPista = 0;
	private HashMap<Integer, Proceso> pendingProcesses;

	public HD(HashMap<Integer, Proceso> listaProcesos) {
		pendingProcesses = listaProcesos;
	}

	public boolean isReading() {
		return isReading;
	}

	public synchronized void leePista(int currentPista) throws InterruptedException {
		while (isReading && this.currentPista != currentPista)
			wait();
		if (this.currentPista != currentPista) {
			this.currentPista = currentPista;
			isReading = true;
			System.out.println("Reading " + currentPista);
			notifyAll();
		} else {
			System.out.println("Sharing " + currentPista);
		}
		wait();
	}

	public synchronized void leer() {
		while (!pendingProcesses.isEmpty()) {
			try {
				while (!isReading && !hasFinalized) {
					wait();
				}
				Thread.currentThread().sleep((int) (Math.random() * 1000 + 1000));
				System.out.println("Read " + currentPista);
				isReading = false;
				notifyAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void setHasFinalized(boolean hasFinalized) {
		this.hasFinalized = hasFinalized;
		notify();
	}
}

class Proceso extends Thread {
	private Integer num;
	private HD hardDisk;
	private int[] pistas;

	public Proceso(Integer num, HD hardDisk) {
		this.num = num;
		this.hardDisk = hardDisk;
		pistas = new int[10];
		for (int i = 0; i < pistas.length; i++) {
			pistas[i] = (int) (Math.random() * 20 + 1);
		}
	}

	public void run() {
		for (int i = 0; i < pistas.length; i++) {
			try {
				sleep((int) (Math.random() * 1000));
				hardDisk.leePista(pistas[i]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finaliza el proceso " + num);
	}
}

class Needle extends Thread {
	private HD hardDisk;

	public Needle(HD hardDisk) {
		this.hardDisk = hardDisk;
	}

	public void run() {
		hardDisk.leer();
		System.out.println("Finaliza el needle ");
	}
}
