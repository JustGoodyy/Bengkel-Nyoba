package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Nota;
import model.NotaItem;
import model.Pengeluaran;
import model.SparePart;
import util.AnimationUtil;
import util.XmlDatabase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class CashierController {

    // --- Cart / Receipt section ---
    @FXML private ComboBox<SparePart> cbSparepart;
    @FXML private TextField txtQty;
    @FXML private TableView<NotaItem> tableCart;
    @FXML private TableColumn<NotaItem, String> colCartNama;
    @FXML private TableColumn<NotaItem, Integer> colCartQty;
    @FXML private TableColumn<NotaItem, Double> colCartHarga;
    @FXML private TableColumn<NotaItem, Double> colCartSubtotal;
    @FXML private TextField txtCustomer;
    @FXML private DatePicker dpTanggalNota;
    @FXML private TextField txtJasaMekanik;
    @FXML private Label lblTotal;
    @FXML private Label lblStatus;
    @FXML private Button btnAddToCart;
    @FXML private Button btnSimpanNota;

    @FXML private TextField txtKeteranganBiaya;
    @FXML private ComboBox<String> cbKategoriBiaya;
    @FXML private DatePicker dpTanggalBiaya;
    @FXML private TextField txtJumlahBiaya;
    @FXML private Label lblStatusBiaya;

    private final XmlDatabase<SparePart> sparepartDb = new XmlDatabase<>("data/sparepart.xml", SparePart.class);
    private final XmlDatabase<Nota> notaDb = new XmlDatabase<>("data/nota.xml", Nota.class);
    private final XmlDatabase<Pengeluaran> pengeluaranDb = new XmlDatabase<>("data/pengeluaran.xml", Pengeluaran.class);

    private ArrayList<SparePart> spareparts;
    private final ObservableList<NotaItem> cart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        spareparts = sparepartDb.loadAll();
        cbSparepart.setItems(FXCollections.observableArrayList(spareparts)); 

        colCartNama.setCellValueFactory(new PropertyValueFactory<>("namaPart"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colCartHarga.setCellValueFactory(new PropertyValueFactory<>("hargaSatuan"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tableCart.setItems(cart);

        cbKategoriBiaya.setItems(FXCollections.observableArrayList(
                "Listrik", "Gaji", "Sewa", "Perawatan Alat", "Lain-lain"));

        dpTanggalNota.setValue(LocalDate.now());
        dpTanggalBiaya.setValue(LocalDate.now());

        // Recompute total any time cart or jasa mekanik fee changes (real-time total)
        cart.addListener((javafx.collections.ListChangeListener<NotaItem>) c -> recomputeTotal());
        txtJasaMekanik.textProperty().addListener((obs, o, n) -> recomputeTotal());
    }

    @FXML
    public void handleAddToCart() {
        AnimationUtil.pulse(btnAddToCart);
        SparePart selected = cbSparepart.getValue();
        if (selected == null) {
            lblStatus.setText("Pilih spare part terlebih dahulu.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (Exception e) {
            lblStatus.setText("Qty harus berupa angka.");
            return;
        }
        if (qty <= 0) {
            lblStatus.setText("Qty harus lebih dari 0.");
            return;
        }
        if (qty > selected.getStok()) {
            lblStatus.setText("Stok tidak mencukupi (sisa " + selected.getStok() + ").");
            return;
        }

        cart.add(new NotaItem(selected.getKode(), selected.getNama(), qty, selected.getHargaJual()));
        txtQty.clear();
        lblStatus.setText("");
    }

    @FXML
    public void handleRemoveFromCart() {
        NotaItem selected = tableCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cart.remove(selected);
        }
    }

    private void recomputeTotal() {
        double totalItems = cart.stream().mapToDouble(NotaItem::getSubtotal).sum();
        double jasa = 0;
        try {
            jasa = Double.parseDouble(txtJasaMekanik.getText().trim());
        } catch (Exception ignored) { }
        lblTotal.setText(String.format("Rp %,.0f", totalItems + jasa));
    }

    @FXML
    public void handleSimpanNota() {
        AnimationUtil.pulse(btnSimpanNota);
        if (cart.isEmpty()) {
            lblStatus.setText("Keranjang masih kosong.");
            return;
        }
        if (txtCustomer.getText().trim().isEmpty()) {
            lblStatus.setText("Nama customer wajib diisi.");
            return;
        }

        double jasa;
        try {
            jasa = txtJasaMekanik.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtJasaMekanik.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("Biaya jasa mekanik harus berupa angka.");
            return;
        }

        if (dpTanggalNota.getValue() == null) {
            lblStatus.setText("Pilih tanggal transaksi terlebih dahulu.");
            return;
        }

        Nota nota = new Nota(
                "NOTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                dpTanggalNota.getValue(),
                txtCustomer.getText().trim(),
                jasa,
                new ArrayList<>(cart)
        );
        ArrayList<Nota> allNota = notaDb.loadAll();
        allNota.add(nota);
        notaDb.saveAll(allNota);

        for (NotaItem item : cart) {
            for (SparePart sp : spareparts) {
                if (sp.getKode().equals(item.getKodePart())) {
                    sp.setStok(sp.getStok() - item.getQty());
                }
            }
        }
        sparepartDb.saveAll(spareparts);

        cart.clear();
        txtCustomer.clear();
        txtJasaMekanik.clear();
        dpTanggalNota.setValue(LocalDate.now());
        cbSparepart.setItems(FXCollections.observableArrayList(spareparts));
        lblStatus.setText("Nota " + nota.getIdNota() + " tersimpan. Total Rp " + String.format("%,.0f", nota.getTotalBayar()));
    }

    @FXML
    public void handleSimpanBiaya() {
        if (txtKeteranganBiaya.getText().trim().isEmpty() || cbKategoriBiaya.getValue() == null || txtJumlahBiaya.getText().trim().isEmpty()) {
            lblStatusBiaya.setText("Semua field biaya wajib diisi.");
            return;
        }
        if (dpTanggalBiaya.getValue() == null) {
            lblStatusBiaya.setText("Pilih tanggal pengeluaran terlebih dahulu.");
            return;
        }
        double jumlah;
        try {
            jumlah = Double.parseDouble(txtJumlahBiaya.getText().trim());
        } catch (NumberFormatException e) {
            lblStatusBiaya.setText("Jumlah harus berupa angka.");
            return;
        }

        Pengeluaran p = new Pengeluaran(
                "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                dpTanggalBiaya.getValue(),
                txtKeteranganBiaya.getText().trim(),
                cbKategoriBiaya.getValue(),
                jumlah
        );
        ArrayList<Pengeluaran> all = pengeluaranDb.loadAll();
        all.add(p);
        pengeluaranDb.saveAll(all);

        txtKeteranganBiaya.clear();
        cbKategoriBiaya.setValue(null);
        txtJumlahBiaya.clear();
        dpTanggalBiaya.setValue(LocalDate.now());
        lblStatusBiaya.setText("Pengeluaran tercatat.");
    }
}
