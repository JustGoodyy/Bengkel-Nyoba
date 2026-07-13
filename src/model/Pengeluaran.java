package model;

import java.time.LocalDate;

public class Pengeluaran {
    private String idPengeluaran;
    private LocalDate tanggal;
    private String keterangan; 
    private String kategori;
    private double jumlah;

    public Pengeluaran() {}

    public Pengeluaran(String idPengeluaran, LocalDate tanggal, String keterangan, String kategori, double jumlah) {
        this.idPengeluaran = idPengeluaran;
        this.tanggal = tanggal;
        this.keterangan = keterangan;
        this.kategori = kategori;
        this.jumlah = jumlah;
    }

    public String getIdPengeluaran() { return idPengeluaran; }
    public LocalDate getTanggal() { return tanggal; }
    public String getKeterangan() { return keterangan; }
    public String getKategori() { return kategori; }
    public double getJumlah() { return jumlah; }

    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setJumlah(double jumlah) { this.jumlah = jumlah; }
}
