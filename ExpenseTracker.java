import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Scanner;

public class ExpenseTracker {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/expense_tracker";
        String user = "root";
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isEmpty()) {
            System.out.println("Set the DB_PASSWORD environment variable first.");
            return;
        }

        try (Connection con =
                 DriverManager.getConnection(url, user, password)) {

            System.out.println("MySQL connected successfully!");

            while (true) {
                System.out.println("\n===== EXPENSE TRACKER =====");
                System.out.println("1. Add Expense");
                System.out.println("2. Display Expenses by Month");
                System.out.println("3. Category-wise Totals");
                System.out.println("4. Update Expense");
                System.out.println("5. Delete Expense");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");

                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1:
                        addExpense(con);
                        break;
                    case 2:
                        displayMonth(con);
                        break;
                    case 3:
                        categoryTotals(con);
                        break;
                    case 4:
                        updateExpense(con);
                        break;
                    case 5:
                        deleteExpense(con);
                        break;
                    case 6:
                        System.out.println("Thank you!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void addExpense(Connection con) throws SQLException {
        System.out.print("Date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(sc.nextLine());

        System.out.print("Category: ");
        String category = sc.nextLine();

        System.out.print("Description: ");
        String description = sc.nextLine();

        System.out.print("Amount: ");
        BigDecimal amount = new BigDecimal(sc.nextLine());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        String sql = "INSERT INTO Expenses " +
            "(expense_date, category, description, amount) " +
            "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setBigDecimal(4, amount);
            ps.executeUpdate();
            System.out.println("Expense added successfully!");
        }
    }

    static void displayMonth(Connection con) throws SQLException {
        System.out.print("Enter month (YYYY-MM): ");
        YearMonth month = YearMonth.parse(sc.nextLine());

        String sql = "SELECT * FROM Expenses " +
            "WHERE expense_date >= ? AND expense_date < ? " +
            "ORDER BY expense_date";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(month.atDay(1)));
            ps.setDate(2, Date.valueOf(month.plusMonths(1).atDay(1)));

            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal total = BigDecimal.ZERO;

                while (rs.next()) {
                    System.out.println(
                        rs.getInt("expense_id") + " | " +
                        rs.getDate("expense_date") + " | " +
                        rs.getString("category") + " | " +
                        rs.getString("description") + " | Rs." +
                        rs.getBigDecimal("amount")
                    );
                    total = total.add(rs.getBigDecimal("amount"));
                }

                System.out.println("Monthly Total: Rs." + total);
            }
        }
    }

    static void categoryTotals(Connection con) throws SQLException {
        String sql = "SELECT category, SUM(amount) AS total " +
                     "FROM Expenses GROUP BY category";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(
                    rs.getString("category") + " : Rs." +
                    rs.getBigDecimal("total")
                );
            }
        }
    }

    static void updateExpense(Connection con) throws SQLException {
        System.out.print("Enter expense ID to update: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("New date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(sc.nextLine());

        System.out.print("New category: ");
        String category = sc.nextLine();

        System.out.print("New description: ");
        String description = sc.nextLine();

        System.out.print("New amount: ");
        BigDecimal amount = new BigDecimal(sc.nextLine());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        String sql = "UPDATE Expenses SET expense_date=?, category=?, " +
                     "description=?, amount=? WHERE expense_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, category);
            ps.setString(3, description);
            ps.setBigDecimal(4, amount);
            ps.setInt(5, id);

            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Expense updated!" :
                               "Expense ID not found.");
        }
    }

    static void deleteExpense(Connection con) throws SQLException {
        System.out.print("Enter expense ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM Expenses WHERE expense_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            System.out.println(rows > 0 ? "Expense deleted!" :
                               "Expense ID not found.");
        }
    }
}