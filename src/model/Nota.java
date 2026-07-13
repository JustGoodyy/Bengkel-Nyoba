package model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Nota {
    private String idNota;
    private LocalDate tanggal;
    private String namaCustomer;
    private double biayaJasaMekanik;
    private ArrayList<NotaItem> items;
    private double totalBayar;

    public Nota() {}

    public Nota(String idNota, LocalDate tanggal, String namaCustomer,
                double biayaJasaMekanik, ArrayList<NotaItem> items) {
        this.idNota = idNota;
        this.tanggal = tanggal;
        this.namaCustomer = namaCustomer;
        this.biayaJasaMekanik = biayaJasaMekanik;
        this.items = items;
        recomputeTotal();
    }

    public String getIdNota() { return idNota; }
    public LocalDate getTanggal() { return tanggal; }
    public String getNamaCustomer() { return namaCustomer; }
    public double getBiayaJasaMekanik() { return biayaJasaMekanik; }
    public ArrayList<NotaItem> getItems() { return items; }
    public double getTotalBayar() { return totalBayar; }

    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }
    public void setBiayaJasaMekanik(double biayaJasaMekanik) { this.biayaJasaMekanik = biayaJasaMekanik; }

    public void recomputeTotal() {
        double totalItems = 0;
        if (items != null) {
            for (NotaItem it : items) totalItems += it.getSubtotal();
        }
        this.totalBayar = totalItems + biayaJasaMekanik;
    }
}
