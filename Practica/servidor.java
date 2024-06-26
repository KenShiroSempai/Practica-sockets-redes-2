import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class servidor {
    private static final String DB_URL = "jdbc:postgresql://db:5432/carrito";
    private static final String DB_USER = "sooyaaahri";
    private static final String DB_PASSWORD = "sooyaaahri";
    // private static final int PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
    private static final int PORT = 6030;
    private static Map<String, Map<String, Object>> catalog = new HashMap<>();

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al registrar el driver de PostgreSQL:");
            e.printStackTrace();
        }
        loadCatalog();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket);

                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void saveCatalog() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("catalogo.txt"))) {
            out.writeObject(catalog);
            System.out.println("Catálogo guardado en catalogo.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadCatalog() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("catalogo.txt"))) {
        catalog = (Map<String, Map<String, Object>>) in.readObject();
        System.out.println("Catálogo cargado desde catalogo.txt");
    } catch (IOException | ClassNotFoundException e) {
        System.out.println("No se encontró el archivo catalogo.txt. Se creará un nuevo catálogo.");
        catalog = new HashMap<>();

        Map<String, Object> product1 = new HashMap<>();
        product1.put("price", 10.0);
        product1.put("quantity", 10);
        catalog.put("Manzana", product1);

        Map<String, Object> product2 = new HashMap<>();
        product2.put("price", 20.0);
        product2.put("quantity", 20);
        catalog.put("Besos", product2);

        Map<String, Object> product3 = new HashMap<>();
        product3.put("price", 30.0);
        product3.put("quantity", 30);
        catalog.put("Manteconchas de chocolate con forma de dinosaurio con corazon", product3);

        saveCatalog();
    }
}
    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
        
                out.writeObject(catalog);
                out.flush();
        
                // Procesa las solicitudes del cliente
                while (true) {
                    Object request = in.readObject();
                    if (request instanceof Map) {
                        catalog = (Map<String, Map<String, Object>>) request;
                        saveCatalog();
                        System.out.println("Catálogo actualizado.");
                    }
                }
        
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}