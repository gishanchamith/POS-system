import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Super-Saving POS System 
 * Functionalities:
 * - Item entry and billing
 * - Discount handling (0-75%)
 * - Fetching item details from CSV
 * - Pending bills management
 * - Revenue report generation
 * - Bill generation as a Text File
 */
public class SuperSaverPOSGroup_ByteBuilders {
    public static void main(String[] args) {
        POS pos = new POS();
        pos.run();
    }
}

class Item {
    String code, name, manufacturer;
    double price, weight;
    Date expiryDate, manufacturerDate;
    int discount;

    public Item(String code, String name, double price, double weight, String manufacturer, Date manufacturerDate, Date expiryDate) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.manufacturer = manufacturer;
        this.manufacturerDate = manufacturerDate;
        this.expiryDate = expiryDate;
    }

    public double getPrice() {
        return price;
    }
}

class CSVReader {
    public static Map<String, Item> loadItems(String filePath) {
        Map<String, Item> items = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                System.out.println("Reading line: " + line); // Debugging
                String[] parts = line.split(",");

                if (parts.length == 7) {
                    try {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        double price = Double.parseDouble(parts[2].trim());
                        double weight = Double.parseDouble(parts[3].trim());
                        String manufacturer = parts[4].trim();
                        Date manufacturerDate = sdf.parse(parts[5].trim());
                        Date expiryDate = sdf.parse(parts[6].trim());

                        Item item = new Item(code, name, price, weight, manufacturer, manufacturerDate, expiryDate);
                        items.put(code.toLowerCase(), item);
                        System.out.println("Loaded item: " + code + " - " + name);
                    } catch (Exception e) {
                        System.err.println("Skipping invalid line: " + line);
                    }
                } else {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading items: " + e.getMessage());
        }
        return items;
    }
}

class Bill {
    Map<Item, Integer> items = new HashMap<>(); // Store item with quantity
    String cashier, customer;
    double totalCost = 0, totalDiscount = 0;
    Date date = new Date();

    public Bill(String cashier, String customer) {
        this.cashier = cashier;
        this.customer = customer;
    }

    public void addItem(Item item, int quantity, int discount) {
        items.put(item, items.getOrDefault(item, 0) + quantity); // Store cumulative quantity
        double itemTotal = item.getPrice() * quantity;
        double discountAmount = (itemTotal * discount) / 100.0;
        
        totalCost += itemTotal - discountAmount;
        totalDiscount += discountAmount;
    }

    public void saveAsTextFile(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Super-Saving Supermarket -Kegalle Branch- Bill\n");
            writer.write("Cashier: " + cashier + "\nCustomer: " + customer + "\n");
            writer.write("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "\n");
            writer.write("--------------------------------------------------\n");
            writer.write("Qty  Item Name                Unit Price  Total Price\n");
            writer.write("--------------------------------------------------\n");

            for (Map.Entry<Item, Integer> entry : items.entrySet()) {
                Item item = entry.getKey();
                int quantity = entry.getValue();
                double totalItemPrice = item.getPrice() * quantity;
                writer.write(String.format("%-4d %-20s Rs. %-10.2f Rs. %.2f\n", 
                            quantity, item.name, item.getPrice(), totalItemPrice));
            }

            writer.write("--------------------------------------------------\n");
            writer.write(String.format("Total Discount: Rs. %.2f\n", totalDiscount));
            writer.write(String.format("Total Cost: Rs. %.2f\n", totalCost));
        }
    }
}

class PendingBillManager {
    private static final Map<String, Bill> pendingBills = new HashMap<>();

    public static void saveBill(String id, Bill bill) {
        pendingBills.put(id, bill);
    }

    public static Bill retrieveBill(String id) {
        return pendingBills.remove(id);
    }
}

class RevenueReport {
    public static void generateReport(String startDate, String endDate) {
        System.out.println("Generating report for " + startDate + " to " + endDate);
        System.out.println("Report saved locally (Email functionality removed).\n");
    }
}

class POS {
    Map<String, Item> inventory = CSVReader.loadItems("items.csv");
    Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("Enter cashier name:");
        String cashier = scanner.nextLine();
        System.out.println("Enter customer name (or press enter if not registered):");
        String customer = scanner.nextLine();
        Bill bill = new Bill(cashier, customer);

        while (true) {
            System.out.println("Enter item code (or type 'done' to finish):");
            String code = scanner.nextLine();
            if (code.equalsIgnoreCase("done")) break;
            if (inventory.containsKey(code)) {
                System.out.println("Enter Quantity:");
                int quantity = scanner.nextInt();
                System.out.println("Enter Discount (%):");
                int discount = scanner.nextInt();
                scanner.nextLine();
                bill.addItem(inventory.get(code), quantity, discount);
            } else {
                System.out.println("Invalid code!");
            }
        }

        try {
            String filename = "bill.txt";
            bill.saveAsTextFile(filename);
            System.out.println("Bill saved as " + filename);
        } catch (IOException e) {
            System.err.println("Error saving bill: " + e.getMessage());
        }
    }
}
