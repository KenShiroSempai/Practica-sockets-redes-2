import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8030;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
    
            Map<String, Map<String, Object>> catalog = (Map<String, Map<String, Object>>) in.readObject();
    
            Map<String, Integer> cart = new HashMap<>();
            Scanner scanner = new Scanner(System.in);
    
            while (true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Ver catálogo");
                System.out.println("2. Agregar producto al catálogo");
                System.out.println("3. Agregar producto al carrito");
                System.out.println("4. Eliminar producto del carrito");
                System.out.println("5. Ver carrito");
                System.out.println("6. Realizar compra");
                System.out.println("7. Salir");
    
                System.out.print("Ingrese una opción: ");
                int option = scanner.nextInt();
    
                switch (option) {
                    case 1:
                        viewCatalog(catalog);
                        break;
                    case 2:
                        addProductToCatalog(catalog, scanner, out);
                        break;
                    case 3:
                        System.out.print("Ingrese el nombre del producto: ");
                        String productName = scanner.next();
                        System.out.print("Ingrese la cantidad: ");
                        int quantity = scanner.nextInt();
                        addToCart(cart, productName, quantity, catalog, scanner);
                        break;
                    case 4:
                        System.out.print("Ingrese el nombre del producto: ");
                        productName = scanner.next();
                        removeFromCart(cart, productName);
                        break;
                    case 5:
                        viewCart(cart, catalog);
                        break;
                    case 6:
                        makePayment(cart, catalog, scanner, out);
                        break;
                    case 7:
                        System.out.println("Gracias por comprar con nosotros.");
                        System.exit(0);
                    default:
                        System.out.println("Opción inválida. Por favor, intente nuevamente.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    

    private static void addToCart(Map<String, Integer> cart, String productName, int quantity, Map<String, Map<String, Object>> catalog, Scanner scanner) {
        if (catalog.containsKey(productName)) {
            Map<String, Object> productDetails = catalog.get(productName);
            int availableQuantity = (int) productDetails.get("quantity");
            if (quantity <= availableQuantity) {
                cart.put(productName, cart.getOrDefault(productName, 0) + quantity);
                System.out.println("Producto agregado al carrito.");
            } else {
                System.out.println("No hay suficiente cantidad disponible en el catálogo.");
            }
        } else {
            System.out.println("El producto no existe en el catálogo.");
        }
    }

    private static void removeFromCart(Map<String, Integer> cart, String productName) {
        if (cart.containsKey(productName)) {
            cart.remove(productName);
            System.out.println("Producto eliminado del carrito.");
        } else {
            System.out.println("El producto no está en el carrito.");
        }
    }

    private static void viewCart(Map<String, Integer> cart, Map<String, Map<String, Object>> catalog) {
        System.out.println("Carrito de compras:");
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();
            Map<String, Object> productDetails = catalog.get(productName);
            if (productDetails != null) {
                double price = (double) productDetails.get("price");
                System.out.println(productName + " - Cantidad: " + quantity + " - Precio: $" + price);
            } else {
                System.out.println(productName + " - Cantidad: " + quantity + " - Precio: No disponible");
            }
        }
    }

    private static void makePayment(Map<String, Integer> cart, Map<String, Map<String, Object>> catalog, Scanner scanner, ObjectOutputStream out) throws IOException {
        if (!cart.isEmpty()) {
            System.out.println("Resumen de compra:");
            double total = 0;
            Map<String, Double> purchasedProducts = new HashMap<>();
            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String productName = entry.getKey();
                int quantity = entry.getValue();
                Map<String, Object> productDetails = catalog.get(productName);
                int availableQuantity = (int) productDetails.get("quantity");
                if (quantity <= availableQuantity) {
                    double price = (double) productDetails.get("price");
                    total += price * quantity;
                    productDetails.put("quantity", availableQuantity - quantity);
                    purchasedProducts.put(productName, price);
                    System.out.println(productName + " - Cantidad: " + quantity + " - Precio: $" + price);
                } else {
                    System.out.println("No hay suficiente cantidad disponible para " + productName);
                }
            }
            System.out.println("Total a pagar: $" + total);
    
            generatePdfReceipt(cart, purchasedProducts, total);
    
            out.writeObject(catalog);
            out.flush();
    
            cart.clear();
            System.out.println("Compra realizada con éxito. Catálogo actualizado.");
        } else {
            System.out.println("No hay productos en el carrito.");
        }
    }

    private static void generatePdfReceipt(Map<String, Integer> cart, Map<String, Double> purchasedProducts, double total) {
        // Implementa la generación del recibo en PDF aquí
        System.out.println("Generando recibo en PDF...");
    }

    private static void viewCatalog(Map<String, Map<String, Object>> catalog) {
        System.out.println("Catálogo de productos:");
        for (Map.Entry<String, Map<String, Object>> entry : catalog.entrySet()) {
            String productName = entry.getKey();
            Map<String, Object> productDetails = entry.getValue();
            double price = (double) productDetails.get("price");
            int quantity = (int) productDetails.get("quantity");
            System.out.println(productName + " - Precio: $" + price + " - Cantidad: " + quantity);
        }
    }
    
    private static void addProductToCatalog(Map<String, Map<String, Object>> catalog, Scanner scanner, ObjectOutputStream out) throws IOException {
        System.out.print("Ingrese el nombre del producto: ");
        String productName = scanner.next();
        System.out.print("Ingrese el precio del producto: ");
        double price = scanner.nextDouble();
        System.out.print("Ingrese la cantidad disponible: ");
        int quantity = scanner.nextInt();
    
        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("price", price);
        productDetails.put("quantity", quantity);
        catalog.put(productName, productDetails);
    
        out.writeObject(catalog);
        out.flush();
    
        System.out.println("Producto agregado al catálogo.");
    }

    private static double getPriceFromCatalog(Map<String, Integer> catalog, String productName) {
        // Implementa la lógica para obtener el precio de un producto del catálogo
        // Puedes modificar esto según la estructura de tu catálogo
        return 0.0;
    }
}