package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.SparePart;
import util.AnimationUtil;
import util.XmlDatabase;
import java.util.ArrayList;

public class InventoryController {

    @FXML private TableView<SparePart> tableSparepart;
    @FXML private TableColumn<SparePart, String> colKode;
    @FXML private TableColumn<SparePart, String> colNama;
    @FXML private TableColumn<SparePart, String> colKategori;
    @FXML private TableColumn<SparePart, Integer> colStok;
    @FXML private TableColumn<SparePart, Double> colHarga;

    @FXML private TextField txtSearch;

    @FXML private TextField txtKode;
    @FXML private TextField txtNama;
    @FXML private ComboBox<String> cbKategori;
    @FXML private TextField txtStok;
    @FXML private TextField txtHargaJual;
    @FXML private TextField txtHargaModal;
    @FXML private Label lblFormStatus;

    @FXML private javafx.scene.control.Button btnAdd;
    @FXML private javafx.scene.control.Button btnUpdate;
    @FXML private javafx.scene.control.Button btnDelete;

    private final XmlDatabase<SparePart> db = new XmlDatabase<>("data/sparepart.xml", SparePart.class);

    private ArrayList<SparePart> masterList;
    private final ObservableList<SparePart> observableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colKode.setCellValueFactory(new PropertyValueFactory<>("kode"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));

        cbKategori.setItems(FXCollections.observableArrayList(
                "Mesin", "Kelistrikan", "Ban & Roda", "Rem", "Oli & Cairan", "Aksesoris"));

        tableSparepart.setItems(observableList);

        tableSparepart.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillForm(newSel);
        });

        refreshFromDisk();

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
    }

    private void refreshFromDisk() {
        masterList = db.loadAll();
        observableList.setAll(masterList);
    }

    private void applyFilter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            observableList.setAll(masterList);
            return;
        }
        String kw = keyword.toLowerCase();
        ArrayList<SparePart> filtered = new ArrayList<>();
        for (SparePart sp : masterList) {
            if (sp.getKode().toLowerCase().contains(kw) || sp.getNama().toLowerCase().contains(kw)) {
                filtered.add(sp);
            }
        }
        observableList.setAll(filtered);
    }

    @FXML
    public void handleAdd() {
        AnimationUtil.pulse(btnAdd);
        if (!validateForm()) return;

        String kode = txtKode.getText().trim();
        for (SparePart sp : masterList) {
            if (sp.getKode().equalsIgnoreCase(kode)) {
                lblFormStatus.setText("Kode sudah dipakai. Gunakan kode lain.");
                return;
            }
        }

        SparePart baru = new SparePart(
                kode,
                txtNama.getText().trim(),
                cbKategori.getValue(),
                Integer.parseInt(txtStok.getText().trim()),
                Double.parseDouble(txtHargaJual.getText().trim()),
                parseModalOrDefault()
        );

        masterList.add(baru);
        db.saveAll(masterList);
        observableList.setAll(masterList);
        clearForm();
        lblFormStatus.setText("Item berhasil ditambahkan.");
    }

    @FXML
    public void handleUpdate() {
        AnimationUtil.pulse(btnUpdate);
        SparePart selected = tableSparepart.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblFormStatus.setText("Pilih item pada tabel terlebih dahulu.");
            return;
        }
        if (!validateForm()) return;

        selected.setNama(txtNama.getText().trim());
        selected.setKategori(cbKategori.getValue());
        selected.setStok(Integer.parseInt(txtStok.getText().trim()));
        selected.setHargaJual(Double.parseDouble(txtHargaJual.getText().trim()));
        selected.setHargaModal(parseModalOrDefault());

        db.saveAll(masterList);
        observableList.setAll(masterList);
        lblFormStatus.setText("Item berhasil diperbarui.");
    }

    @FXML
    public void handleDelete() {
        AnimationUtil.pulse(btnDelete);
        SparePart selected = tableSparepart.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblFormStatus.setText("Pilih item pada tabel terlebih dahulu.");
            return;
        }
        masterList.remove(selected);
        db.saveAll(masterList);
        observableList.setAll(masterList);
        clearForm();
        lblFormStatus.setText("Item berhasil dihapus.");
    }

    @FXML
    public void handleClear() {
        clearForm();
    }

    private void fillForm(SparePart sp) {
        txtKode.setText(sp.getKode());
        txtKode.setDisable(true); // kode is the primary key: not editable after creation
        txtNama.setText(sp.getNama());
        cbKategori.setValue(sp.getKategori());
        txtStok.setText(String.valueOf(sp.getStok()));
        txtHargaJual.setText(String.valueOf(sp.getHargaJual()));
        txtHargaModal.setText(String.valueOf(sp.getHargaModal()));
    }

    private void clearForm() {
        txtKode.clear();
        txtKode.setDisable(false);
        txtNama.clear();
        cbKategori.setValue(null);
        txtStok.clear();
        txtHargaJual.clear();
        txtHargaModal.clear();
        tableSparepart.getSelectionModel().clearSelection();
        lblFormStatus.setText("");
    }

    private double parseModalOrDefault() {
        try {
            if (!txtHargaModal.getText().trim().isEmpty()) {
                return Double.parseDouble(txtHargaModal.getText().trim());
            }
        } catch (NumberFormatException ignored) { }
        return Double.parseDouble(txtHargaJual.getText().trim()) * 0.6;
    }

    private boolean validateForm() {
        if (txtKode.getText().trim().isEmpty() || txtNama.getText().trim().isEmpty()
                || cbKategori.getValue() == null || txtStok.getText().trim().isEmpty()
                || txtHargaJual.getText().trim().isEmpty()) {
            lblFormStatus.setText("Semua field wajib diisi.");
            return false;
        }
        try {
            Integer.parseInt(txtStok.getText().trim());
            Double.parseDouble(txtHargaJual.getText().trim());
        } catch (NumberFormatException e) {
            lblFormStatus.setText("Stok dan Harga harus berupa angka.");
            return false;
        }
        return true;
    }
}
