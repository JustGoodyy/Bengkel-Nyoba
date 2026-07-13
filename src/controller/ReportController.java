package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.JurnalHarian;
import model.Nota;
import model.NotaItem;
import model.Pengeluaran;
import model.SparePart;
import util.AnimationUtil;
import util.XmlDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class ReportController {

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private javafx.scene.control.Button btnGenerate;

    @FXML private Label lblTotalIncome;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblNetProfit;

    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private PieChart chartExpenseComposition;

    @FXML private TableView<JurnalHarian> tableLedger;
    @FXML private TableColumn<JurnalHarian, LocalDate> colTanggal;
    @FXML private TableColumn<JurnalHarian, String> colKeterangan;
    @FXML private TableColumn<JurnalHarian, String> colJenis;
    @FXML private TableColumn<JurnalHarian, Double> colMasuk;
    @FXML private TableColumn<JurnalHarian, Double> colKeluar;
    @FXML private TableColumn<JurnalHarian, Void> colAksi;

    private final XmlDatabase<Nota> notaDb = new XmlDatabase<>("data/nota.xml", Nota.class);
    private final XmlDatabase<Pengeluaran> pengeluaranDb = new XmlDatabase<>("data/pengeluaran.xml", Pengeluaran.class);
    private final XmlDatabase<SparePart> sparepartDb = new XmlDatabase<>("data/sparepart.xml", SparePart.class);
    private final XmlDatabase<JurnalHarian> jurnalDb = new XmlDatabase<>("data/jurnal.xml", JurnalHarian.class);

    private static final String JENIS_MASUK = "PEMASUKAN";
    private static final String JENIS_KELUAR = "PENGELUARAN";

    @FXML
    public void initialize() {
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colMasuk.setCellValueFactory(new PropertyValueFactory<>("pemasukan"));
        colKeluar.setCellValueFactory(new PropertyValueFactory<>("pengeluaran"));

        if (colAksi != null) {
            setupAksiColumn();
        }

        dpStart.setValue(LocalDate.now().withDayOfMonth(1));
        dpEnd.setValue(LocalDate.now());

        handleGenerate();
    }
    

    private void setupAksiColumn() {
        colAksi.setCellFactory(col -> new TableCell<JurnalHarian, Void>() {
            private final Button btnEdit = new Button("📝");
            private final Button btnDelete = new Button("🗑");
            private final HBox box = new HBox(12, btnEdit, btnDelete); 

            {
                btnEdit.getStyleClass().add("btn-action-icon");
                btnEdit.setTooltip(new Tooltip("Edit Entri"));
                
                btnDelete.getStyleClass().add("btn-action-icon");
                btnDelete.setStyle("-fx-text-fill: #e53935;"); 
                btnDelete.setTooltip(new Tooltip("Hapus Entri"));

                btnEdit.setOnAction(e -> {
                    JurnalHarian row = getTableView().getItems().get(getIndex());
                    handleEditLedgerEntry(row);
                });
                btnDelete.setOnAction(e -> {
                    JurnalHarian row = getTableView().getItems().get(getIndex());
                    handleDeleteLedgerEntry(row);
                });
                
                box.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    public void handleGenerate() {
        if (btnGenerate != null) AnimationUtil.pulse(btnGenerate);
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();
        if (start == null || end == null || start.isAfter(end)) return;

        ArrayList<Nota> allNota = notaDb.loadAll();
        ArrayList<Pengeluaran> allBiaya = pengeluaranDb.loadAll();
        ArrayList<SparePart> allParts = sparepartDb.loadAll();

        ArrayList<SparePart> partsSortedByKode = new ArrayList<>(allParts);
        partsSortedByKode.sort(Comparator.comparing(SparePart::getKode));

        ArrayList<Nota> notaInRange = new ArrayList<>();
        for (Nota n : allNota) {
            if (!n.getTanggal().isBefore(start) && !n.getTanggal().isAfter(end)) notaInRange.add(n);
        }
        ArrayList<Pengeluaran> biayaInRange = new ArrayList<>();
        for (Pengeluaran p : allBiaya) {
            if (!p.getTanggal().isBefore(start) && !p.getTanggal().isAfter(end)) biayaInRange.add(p);
        }

        double totalIncome = notaInRange.stream().mapToDouble(Nota::getTotalBayar).sum();
        double totalExpenseOps = biayaInRange.stream().mapToDouble(Pengeluaran::getJumlah).sum();

        double totalCogs = 0;
        for (Nota n : notaInRange) {
            for (NotaItem item : n.getItems()) {
                double modal = findHargaModalByKode(partsSortedByKode, item.getKodePart());
                if (modal < 0) {
                    modal = item.getHargaSatuan() * 0.6;
                }
                totalCogs += item.getQty() * modal;
            }
        }
        double totalExpense = totalExpenseOps + totalCogs;
        double netProfit = totalIncome - totalExpense;

        lblTotalIncome.setText(String.format("Rp %,.0f", totalIncome));
        lblTotalExpense.setText(String.format("Rp %,.0f", totalExpense));
        lblNetProfit.setText(String.format("Rp %,.0f", netProfit));

        buildRevenueChart(notaInRange);
        buildExpensePieChart(biayaInRange);
        buildLedger(notaInRange, biayaInRange);
    }

    private double findHargaModalByKode(ArrayList<SparePart> sortedParts, String kode) {
        int lo = 0, hi = sortedParts.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = sortedParts.get(mid).getKode().compareTo(kode);
            if (cmp == 0) {
                return sortedParts.get(mid).getHargaModal();
            } else if (cmp < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return -1;
    }

    private int sequentialSearch(ArrayList<?> keys, Object key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(key)) {
                return i;
            }
        }
        return -1;
    }
 
    private void buildRevenueChart(ArrayList<Nota> notaInRange) {
        ArrayList<LocalDate> perDayDates = new ArrayList<>();
        ArrayList<Double> perDayTotals = new ArrayList<>();
 
        for (Nota n : notaInRange) {
            int idx = sequentialSearch(perDayDates, n.getTanggal());
            if (idx == -1) {
                perDayDates.add(n.getTanggal());
                perDayTotals.add(n.getTotalBayar());
            } else {
                perDayTotals.set(idx, perDayTotals.get(idx) + n.getTotalBayar());
            }
        }

        ArrayList<Integer> order = new ArrayList<>();
        for (int i = 0; i < perDayDates.size(); i++) order.add(i);
        order.sort(Comparator.comparing(perDayDates::get));
 
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pendapatan Harian");
        for (int i : order) {
            series.getData().add(new XYChart.Data<>(perDayDates.get(i).toString(), perDayTotals.get(i)));
        }
 
        chartRevenue.getData().clear();
        chartRevenue.getData().add(series);
    }

    private void buildExpensePieChart(ArrayList<Pengeluaran> biayaInRange) {
        ArrayList<String> categories = new ArrayList<>();
        ArrayList<Double> totals = new ArrayList<>();
 
        for (Pengeluaran p : biayaInRange) {
            int idx = sequentialSearch(categories, p.getKategori());
            if (idx == -1) {
                categories.add(p.getKategori());
                totals.add(p.getJumlah());
            } else {
                totals.set(idx, totals.get(idx) + p.getJumlah());
            }
        }
 
        ArrayList<PieChart.Data> slices = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            slices.add(new PieChart.Data(categories.get(i), totals.get(i)));
        }
        chartExpenseComposition.setData(FXCollections.observableArrayList(slices));
    }

    private void buildLedger(ArrayList<Nota> notaInRange, ArrayList<Pengeluaran> biayaInRange) {
        ArrayList<JurnalHarian> ledger = new ArrayList<>();
        for (Nota n : notaInRange) {
            ledger.add(new JurnalHarian(n.getTanggal(), "Nota " + n.getIdNota() + " - " + n.getNamaCustomer(),
                    JENIS_MASUK, n.getTotalBayar(), 0, n.getIdNota()));
        }
        for (Pengeluaran p : biayaInRange) {
            ledger.add(new JurnalHarian(p.getTanggal(), p.getKeterangan(), JENIS_KELUAR, 0, p.getJumlah(),
                    p.getIdPengeluaran()));
        }
        ledger.sort((a, b) -> a.getTanggal().compareTo(b.getTanggal()));

        tableLedger.setItems(FXCollections.observableArrayList(ledger));
        jurnalDb.saveAll(ledger);
    }

    private void handleEditLedgerEntry(JurnalHarian entry) {
        if (JENIS_MASUK.equals(entry.getJenis())) {
            editNota(entry.getId());
        } else {
            editPengeluaran(entry.getId());
        }
    }

    private void handleDeleteLedgerEntry(JurnalHarian entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Yakin ingin menghapus entri ini dari jurnal?\n\"" + entry.getKeterangan() + "\""
                + (JENIS_MASUK.equals(entry.getJenis()) ? "\n\nStok spare part terkait akan dikembalikan." : ""));
        Optional<ButtonType> result = confirm.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) return;

        if (JENIS_MASUK.equals(entry.getJenis())) {
            deleteNota(entry.getId());
        } else {
            deletePengeluaran(entry.getId());
        }
        handleGenerate();
    }

    private void editNota(String idNota) {
        ArrayList<Nota> allNota = notaDb.loadAll();
        Nota target = findNota(allNota, idNota);
        if (target == null) {
            showAlert(Alert.AlertType.ERROR, "Data nota tidak ditemukan (mungkin sudah dihapus).");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Nota " + target.getIdNota());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtCustomer = new TextField(target.getNamaCustomer());
        DatePicker dpTgl = new DatePicker(target.getTanggal());
        TextField txtJasa = new TextField(String.valueOf(target.getBiayaJasaMekanik()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Customer:"), txtCustomer);
        grid.addRow(1, new Label("Tanggal:"), dpTgl);
        grid.addRow(2, new Label("Biaya Jasa Mekanik:"), txtJasa);
        Label note = new Label("Catatan: daftar spare part pada nota tidak bisa diubah di sini.\n"
                + "Untuk mengubah item, hapus nota ini lalu buat nota baru di menu Cashier.");
        note.setWrapText(true);
        note.setMaxWidth(320);
        grid.add(note, 0, 3, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) return;

        if (txtCustomer.getText().trim().isEmpty() || dpTgl.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Nama customer dan tanggal wajib diisi.");
            return;
        }
        double jasa;
        try {
            jasa = txtJasa.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtJasa.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Biaya jasa mekanik harus berupa angka.");
            return;
        }

        target.setNamaCustomer(txtCustomer.getText().trim());
        target.setTanggal(dpTgl.getValue());
        target.setBiayaJasaMekanik(jasa);
        target.recomputeTotal();

        notaDb.saveAll(allNota);
        handleGenerate();
    }

    private void deleteNota(String idNota) {
        ArrayList<Nota> allNota = notaDb.loadAll();
        Nota target = findNota(allNota, idNota);
        if (target == null) return;

        ArrayList<SparePart> allParts = sparepartDb.loadAll();
        for (NotaItem item : target.getItems()) {
            for (SparePart sp : allParts) {
                if (sp.getKode().equals(item.getKodePart())) {
                    sp.setStok(sp.getStok() + item.getQty());
                }
            }
        }
        sparepartDb.saveAll(allParts);

        allNota.remove(target);
        notaDb.saveAll(allNota);
    }

    private Nota findNota(ArrayList<Nota> list, String idNota) {
        for (Nota n : list) {
            if (n.getIdNota().equals(idNota)) return n;
        }
        return null;
    }

    private void editPengeluaran(String id) {
        ArrayList<Pengeluaran> all = pengeluaranDb.loadAll();
        Pengeluaran target = findPengeluaran(all, id);
        if (target == null) {
            showAlert(Alert.AlertType.ERROR, "Data pengeluaran tidak ditemukan (mungkin sudah dihapus).");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Pengeluaran");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField txtKet = new TextField(target.getKeterangan());
        ComboBox<String> cbKat = new ComboBox<>(FXCollections.observableArrayList(
                "Listrik", "Gaji", "Sewa", "Perawatan Alat", "Lain-lain"));
        cbKat.setValue(target.getKategori());
        DatePicker dpTgl = new DatePicker(target.getTanggal());
        TextField txtJumlah = new TextField(String.valueOf(target.getJumlah()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Keterangan:"), txtKet);
        grid.addRow(1, new Label("Kategori:"), cbKat);
        grid.addRow(2, new Label("Tanggal:"), dpTgl);
        grid.addRow(3, new Label("Jumlah:"), txtJumlah);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) return;

        if (txtKet.getText().trim().isEmpty() || cbKat.getValue() == null || dpTgl.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Semua field wajib diisi.");
            return;
        }
        double jumlah;
        try {
            jumlah = Double.parseDouble(txtJumlah.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Jumlah harus berupa angka.");
            return;
        }

        target.setKeterangan(txtKet.getText().trim());
        target.setKategori(cbKat.getValue());
        target.setTanggal(dpTgl.getValue());
        target.setJumlah(jumlah);

        pengeluaranDb.saveAll(all);
        handleGenerate();
    }

    private void deletePengeluaran(String id) {
        ArrayList<Pengeluaran> all = pengeluaranDb.loadAll();
        all.removeIf(p -> p.getIdPengeluaran().equals(id));
        pengeluaranDb.saveAll(all);
    }

    private Pengeluaran findPengeluaran(ArrayList<Pengeluaran> list, String id) {
        for (Pengeluaran p : list) {
            if (p.getIdPengeluaran().equals(id)) return p;
        }
        return null;
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}