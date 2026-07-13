package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Nota;
import model.Pengeluaran;
import model.SparePart;
import util.XmlDatabase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.scene.image.ImageView;

public class DashboardController {

    @FXML private TableView<SparePart> tableLowStock;
    @FXML private TableColumn<SparePart, String> colLowStockNama;
    @FXML private TableColumn<SparePart, Integer> colLowStockJumlah;

    @FXML private TableView<Nota> tableRecent;
    @FXML private TableColumn<Nota, String> colRecentId;
    @FXML private TableColumn<Nota, String> colRecentCustomer;
    @FXML private TableColumn<Nota, Double> colRecentTotal;

    @FXML private Label lblCashInToday;
    @FXML private Label lblCashOutToday;
    @FXML private ImageView imglogo;
    private static final int LOW_STOCK_THRESHOLD = 5;

    @FXML
    public void initialize() {
        colLowStockNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colLowStockJumlah.setCellValueFactory(new PropertyValueFactory<>("stok"));

        colRecentId.setCellValueFactory(new PropertyValueFactory<>("idNota"));
        colRecentCustomer.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        colRecentTotal.setCellValueFactory(new PropertyValueFactory<>("totalBayar"));

        loadLowStockWarning();
        loadRecentTransactions();
        loadTodaySummary();
    }

    private void loadLowStockWarning() {
        ArrayList<SparePart> all = new XmlDatabase<SparePart>("data/sparepart.xml", SparePart.class).loadAll();
        ArrayList<SparePart> lowStock = new ArrayList<>();
        for (SparePart sp : all) {
            if (sp.getStok() < LOW_STOCK_THRESHOLD) lowStock.add(sp);
        }
        tableLowStock.setItems(FXCollections.observableArrayList(lowStock));
    }

    private void loadRecentTransactions() {
        ArrayList<Nota> all = new XmlDatabase<Nota>("data/nota.xml", Nota.class).loadAll();
        all.sort(Comparator.comparing(Nota::getTanggal).reversed());
        ArrayList<Nota> last5 = new ArrayList<>(all.subList(0, Math.min(5, all.size())));
        tableRecent.setItems(FXCollections.observableArrayList(last5));
    }

    private void loadTodaySummary() {
        LocalDate today = LocalDate.now();

        ArrayList<Nota> allNota = new XmlDatabase<Nota>("data/nota.xml", Nota.class).loadAll();
        double cashIn = allNota.stream()
                .filter(n -> n.getTanggal().equals(today))
                .mapToDouble(Nota::getTotalBayar)
                .sum();

        ArrayList<Pengeluaran> allBiaya = new XmlDatabase<Pengeluaran>("data/pengeluaran.xml", Pengeluaran.class).loadAll();
        double cashOut = allBiaya.stream()
                .filter(p -> p.getTanggal().equals(today))
                .mapToDouble(Pengeluaran::getJumlah)
                .sum();

        lblCashInToday.setText(String.format("Rp %,.0f", cashIn));
        lblCashOutToday.setText(String.format("Rp %,.0f", cashOut));
    }
}
