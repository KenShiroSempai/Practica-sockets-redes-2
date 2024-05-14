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
        loadCatalogFromDatabase();
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

    private static void loadCatalogFromDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM productos")) {

            while (rs.next()) {
                String productName = rs.getString("nombre");
                double price = rs.getDouble("precio");
                int quantity = rs.getInt("cantidad");

                Map<String, Object> product = new HashMap<>();
                product.put("price", price);
                product.put("quantity", quantity);
                catalog.put(productName, product);
            }
            System.out.println("Catálogo cargado desde la base de datos.");
        } catch (SQLException e) {
            System.out.println("Error al cargar el catálogo desde la base de datos:");
            e.printStackTrace();
        }
    }

    private static void saveCatalogToDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Desactivar el autocommit para manejar la transacción manualmente.
            connection.setAutoCommit(false);

            try (PreparedStatement stmtInsert = connection.prepareStatement("INSERT INTO productos (nombre, precio, cantidad) VALUES (?, ?, ?)");
                 PreparedStatement stmtUpdate = connection.prepareStatement("UPDATE productos SET precio = ?, cantidad = ? WHERE nombre = ?")) {

                for (Map.Entry<String, Map<String, Object>> entry : catalog.entrySet()) {
                    String productName = entry.getKey();
                    Map<String, Object> product = entry.getValue();
                    double price = (double) product.get("price");
                    int quantity = (int) product.get("quantity");

                    // Intenta actualizar el producto.
                    stmtUpdate.setDouble(1, price);
                    stmtUpdate.setInt(2, quantity);
                    stmtUpdate.setString(3, productName);
                    int rowsUpdated = stmtUpdate.executeUpdate();

                    // Si no se actualizó ninguna fila, inserta el producto.
                    if (rowsUpdated == 0) {
                        stmtInsert.setString(1, productName);
                        stmtInsert.setDouble(2, price);
                        stmtInsert.setInt(3, quantity);
                        stmtInsert.executeUpdate();
                    }
                }

                // Confirmar la transacción.
                connection.commit();
                System.out.println("Catálogo guardado en la base de datos.");
            } catch (SQLException e) {
                // En caso de error, revertir la transacción.
                connection.rollback();
                System.out.println("Error al guardar el catálogo en la base de datos:");
                e.printStackTrace();
            } finally {
                // Restaurar el autocommit.
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos:");
            e.printStackTrace();
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
                        saveCatalogToDatabase();
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