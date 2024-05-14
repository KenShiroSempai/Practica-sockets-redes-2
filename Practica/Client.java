import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client {
    private static String serverIP;
    private static int serverPort;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese la dirección IP del servidor: ");
        serverIP = scanner.nextLine();

        System.out.print("Ingrese el puerto del servidor: ");
        serverPort = scanner.nextInt();

        try (Socket socket = new Socket(serverIP, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> catalog = (Map<String, Map<String, Object>>) in.readObject();
            Map<String, Integer> cart = new HashMap<>();

            while (true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Ver catálogo");
                System.out.println("2. Agregar producto al carrito");
                System.out.println("3. Eliminar producto del carrito");
                System.out.println("4. Ver carrito");
                System.out.println("5. Realizar compra");
                System.out.println("6. Salir");

                System.out.print("Ingrese una opción: ");
                int option = scanner.nextInt();
                scanner.nextLine(); 

                switch (option) {
                    case 1:
                        System.out.print(String.format("\033[2J"));
                        viewCatalog(catalog);
                        break;
                    case 2:
                        System.out.print(String.format("\033[2J"));
                        System.out.print("Ingrese el nombre del producto: ");
                        String productName = scanner.nextLine();
                        System.out.print("Ingrese la cantidad: ");
                        int quantity = scanner.nextInt();
                        scanner.nextLine();
                        addToCart(cart, productName, quantity, catalog, scanner);
                        break;
                    case 3:
                        System.out.print(String.format("\033[2J")); 
                        System.out.print("Ingrese el nombre del producto: ");
                        productName = scanner.nextLine();
                        removeFromCart(cart, productName);
                        break;
                    case 4:
                        System.out.print(String.format("\033[2J"));
                        viewCart(cart, catalog);
                        break;
                    case 5:
                        System.out.print(String.format("\033[2J"));
                        makePayment(cart, catalog, scanner, out);
                        break;
                    case 6:
                        System.out.print(String.format("\033[2J"));
                        System.out.println("Gracias por comprar con nosotros.");
                        System.exit(0);
                    default:
                        System.out.print(String.format("\033[2J"));
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

            LocalDateTime hora = LocalDateTime.now();
            String horaString = hora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            try (PrintStream printStream = new PrintStream(new FileOutputStream("ResumenDeCompra.pdf"))) {
                printStream.println("%PDF-1.4");
                printStream.println("1 0 obj");
                printStream.println("<<");
                printStream.println("/Type /Catalog");
                printStream.println("/Pages 2 0 R");
                printStream.println(">>");
                printStream.println("endobj");
                printStream.println("2 0 obj");
                printStream.println("<<");
                printStream.println("/Type /Pages");
                printStream.println("/Kids [3 0 R]");
                printStream.println("/Count 1");
                printStream.println(">>");
                printStream.println("endobj");
                printStream.println("3 0 obj");
                printStream.println("<<");
                printStream.println("/Type /Page");
                printStream.println("/Parent 2 0 R");
                printStream.println("/Resources <<");
                printStream.println("/Font <<");
                printStream.println("/F1 4 0 R");
                printStream.println(">>");
                printStream.println(">>");
                printStream.println("/MediaBox [0 0 595 842]");
                printStream.println("/Contents 5 0 R");
                printStream.println(">>");
                printStream.println("endobj");
                printStream.println("4 0 obj");
                printStream.println("<<");
                printStream.println("/Type /Font");
                printStream.println("/Subtype /Type1");
                printStream.println("/BaseFont /Helvetica");
                printStream.println(">>");
                printStream.println("endobj");
                printStream.println("5 0 obj");
                printStream.println("<<");
                printStream.println("/Length 1000");
                printStream.println(">>");
                printStream.println("stream");

                printStream.println("BT");
                printStream.println("/F1 12 Tf");
                printStream.println("50 800 Td");
                printStream.println("(Resumen de Compra) Tj");
                printStream.println("ET");

                int y = 750;
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

                        printStream.println("BT");
                        printStream.println("/F1 10 Tf");
                        printStream.println("50 " + y + " Td");
                        printStream.println("(" + productName + " - Cantidad: " + quantity + " - Precio: $" + price + ") Tj");
                        printStream.println("ET");

                        y -= 20;

                        System.out.println(productName + " - Cantidad: " + quantity + " - Precio: $" + price);
                    } else {
                        System.out.println("No hay suficiente cantidad disponible para " + productName);
                    }
                }

                printStream.println("BT");
                printStream.println("/F1 10 Tf");
                printStream.println("50 " + (y - 20) + " Td");
                printStream.println("(Total a pagar: $" + total + ") Tj");
                printStream.println("ET");

                printStream.println("BT");
                printStream.println("/F1 10 Tf");
                printStream.println("50 20 Td");
                printStream.println("(Fecha y Hora: " + horaString + ") Tj");
                printStream.println("ET");

                printStream.println("endstream");
                printStream.println("endobj");
                printStream.println("xref");
                printStream.println("0 6");
                printStream.println("0000000000 65535 f");
                printStream.println("0000000010 00000 n");
                printStream.println("0000000079 00000 n");
                printStream.println("0000000173 00000 n");
                printStream.println("0000000301 00000 n");
                printStream.println("0000000380 00000 n");
                printStream.println("trailer");
                printStream.println("<<");
                printStream.println("/Size 6");
                printStream.println("/Root 1 0 R");
                printStream.println(">>");
                printStream.println("startxref");
                printStream.println("1400");
                printStream.println("%%EOF");
            }

            System.out.println("Total a pagar: $" + total);

            out.writeObject(catalog);
            out.flush();

            cart.clear();
            System.out.println("Compra realizada con éxito. Catálogo actualizado.");
            System.out.println("Resumen de compra guardado en ResumenDeCompra.pdf");

        } else {
            System.out.println("No hay productos en el carrito.");
        }
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

}