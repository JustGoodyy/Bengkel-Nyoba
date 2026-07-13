# Bengkel Management System (Workshop Management App)

Untuk Memenuhi Tugas Beasr RPL + FPA + ASD
Kelompok Keren 

## 1. Project Structure

```
BengkelApp/
├── src/
│   ├── app/
│   │   └── Main.java                  # Application entry point, boots Login.fxml
│   ├── model/                         # Plain OOP data classes (no JavaFX deps)
│   │   ├── User.java
│   │   ├── UserSession.java           # Singleton: who is logged in right now
│   │   ├── SparePart.java             # Menu 1 data
│   │   ├── NotaItem.java              # one line inside a receipt
│   │   ├── Nota.java                  # Menu 2 data (service receipt)
│   │   ├── Pengeluaran.java           # Menu 2 data (expense)
│   │   └── JurnalHarian.java          # DTO used only by the Report ledger
│   ├── controller/                    # One controller per FXML view (View<->Model glue)
│   │   ├── LoginController.java
│   │   ├── MainLayoutController.java  # Sidebar navigation shell
│   │   ├── DashboardController.java   # Menu 4 (read-only)
│   │   ├── InventoryController.java   # Menu 1 (full CRUD)
│   │   ├── CashierController.java     # Menu 2 (create-only, cart + expenses)
│   │   └── ReportController.java      # Menu 3 (read-only, charts + ledger)
│   └── util/
│       └── XmlDatabase.java           # Generic XStream <-> ArrayList<T> bridge
│
├── resources/
│   ├── view/                          # FXML (Scene Builder compatible)
│   │   ├── Login.fxml
│   │   ├── MainLayout.fxml
│   │   ├── Dashboard.fxml
│   │   ├── Inventory.fxml
│   │   ├── Cashier.fxml
│   │   └── Report.fxml
│   └── css/
│       └── style.css                  # Blue/white theme (see palette below)
│
├── data/                              # "Database" — created/updated at runtime
│   ├── users.xml                      # seeded with demo accounts
│   ├── sparepart.xml                  # seeded with 4 sample parts
│   ├── nota.xml                       # starts empty, filled by Cashier
│   └── pengeluaran.xml                # starts empty, filled by Cashier
│
├── lib/                               # put xstream-*.jar here (not bundled)
├── compile.sh                         # plain javac build (edit the 2 paths inside)
├── run.sh                             # launches app.Main
└── README.md
```

## 2. Architecture notes (MVC + data flow)

- **Model** (`model/*`) — plain POJOs, zero JavaFX imports, safe to unit test
  and safe for XStream to (de)serialize.
- **View** (`resources/view/*.fxml`) — pure layout, editable in Scene Builder,
  no logic.
- **Controller** (`controller/*`) — the only place that touches both JavaFX
  controls and Model/util classes. Each controller owns exactly one FXML file
  (`fx:controller="controller.XyzController"`).
- **util.XmlDatabase<T>** is the single persistence gateway. Every feature's
  data flow is: `ArrayList<T> (loaded from disk) → ObservableList<T> (bound
  to TableView) → user edits → ArrayList<T> saved back to disk`. This is the
  "ArrayList bridged to ObservableList" requirement from the spec.
- **Sidebar navigation** (`MainLayoutController.loadView`) swaps FXML content
  into a `StackPane` in the center of `MainLayout.fxml` — no new windows are
  opened, matching the requirement.

## 3. Chart <-> XML data flow (Dashboard/Report)

`ReportController.buildRevenueChart()` shows the exact pattern used
everywhere charts read from XML:

```java
ArrayList<Nota> notaInRange = notaDb.loadAll();      // 1. read XML via XStream
Map<LocalDate, Double> perDay = new HashMap<>();
for (Nota n : notaInRange) {
    perDay.merge(n.getTanggal(), n.getTotalBayar(), Double::sum); // 2. aggregate
}
XYChart.Series<String, Number> series = new XYChart.Series<>();
perDay.forEach((date, total) -> series.getData().add(
    new XYChart.Data<>(date.toString(), total)));    // 3. push into the chart
chartRevenue.getData().add(series);
```

The same read → aggregate → push pattern is used for the PieChart (expense
category totals) and the Dashboard's low-stock / recent-transactions tables.

## 4. Assumptions made beyond the original brief

- **COGS / harga modal**: the brief only specifies a selling price for spare
  parts. A `hargaModal` (cost price) field was added to `SparePart` so the
  Financial Report can compute a genuine profit margin. If left blank on the
  Inventory form it defaults to 60% of the selling price.
- **Update/Delete lock on paid Nota**: enforced simply by never exposing
  setters on `Nota`/giving the Cashier screen no edit/delete buttons for
  receipts — only for cart items *before* saving.
- **XStream security**: XStream 1.4.18+ blocks all types by default; the
  project explicitly whitelists what it needs via `AnyTypePermission` for
  simplicity. In a production system you'd scope this down to exactly the
  Model classes.

## 6. Color palette (style.css)

| Token              | Hex       | Used for                          |
|---------------------|-----------|------------------------------------|
| `-fx-primary`        | `#0A58CA` | Primary buttons, focus borders     |
| `-fx-primary-dark`   | `#0056B3` | Headers, table header background   |
| `-fx-bg-light`       | `#F4F6F9` | Page backgrounds                   |
| `-fx-card-white`     | `#FFFFFF` | Cards, tables, login card          |
| `-fx-border-gray`    | `#E1E5EA` | Card/table borders                 |
