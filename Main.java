import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Clase que representa a un trabajador
class Trabajador {
    private String nombre;
    private boolean dentro;

    public Trabajador(String nombre) {
        this.nombre = nombre;
        this.dentro = false;
    }

    public synchronized void entrar() {
        if (!dentro) {
            dentro = true;
            System.out.println(nombre + " ha entrado.");
        } else {
            System.out.println(nombre + " ya está dentro.");
        }
    }

    public synchronized void salir() {
        if (dentro) {
            dentro = false;
            System.out.println(nombre + " ha salido.");
        } else {
            System.out.println(nombre + " ya ha salido.");
        }
    }
}

// Clase que maneja la comunicación con el servidor central
class ClienteCentral {
    private static final String SERVER_IP = "127.0.0.1"; // IP del servidor central
    private static final int SERVER_PORT = 8888; // Puerto del servidor central

    public static void enviarRegistro(String registro) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(registro);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Clase que maneja las solicitudes de entrada y salida de los trabajadores
class GestorTrabajadores implements Runnable {
    private Trabajador trabajador;

    public GestorTrabajadores(Trabajador trabajador) {
        this.trabajador = trabajador;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep((long) (Math.random() * 5000)); // Simula un tiempo de espera aleatorio
                if (Math.random() < 0.5) {
                    trabajador.entrar();
                    ClienteCentral.enviarRegistro(trabajador.toString() + " ha entrado."); // Envía registro al servidor central
                } else {
                    trabajador.salir();
                    ClienteCentral.enviarRegistro(trabajador.toString() + " ha salido."); // Envía registro al servidor central
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Clase principal
public class Main {
    public static void main(String[] args) {
        Trabajador[] trabajadores = new Trabajador[5]; // Crear un array de trabajadores

        // Inicializar trabajadores
        for (int i = 0; i < trabajadores.length; i++) {
            trabajadores[i] = new Trabajador("Trabajador " + (i + 1));
        }

        // Crear pool de hilos
        ExecutorService executor = Executors.newFixedThreadPool(trabajadores.length);

        // Asignar hilos a cada trabajador
        for (Trabajador trabajador : trabajadores) {
            executor.execute(new GestorTrabajadores(trabajador));
        }

        // Apagar el pool de hilos después de un tiempo
        executor.shutdown();
    }
}
