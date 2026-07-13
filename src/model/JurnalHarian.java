package model;

import java.time.LocalDate;

public class JurnalHarian {
    private LocalDate tanggal;
    private String keterangan;
    private String jenis;   
    private double pemasukan;  
    private double pengeluaran; 
    private String id;        

    public JurnalHarian() {}

    public JurnalHarian(LocalDate tanggal, String keterangan, String jenis,
                         double pemasukan, double pengeluaran, String id) {
        this.tanggal = tanggal;
        this.keterangan = keterangan;
        this.jenis = jenis;
        this.pemasukan = pemasukan;
        this.pengeluaran = pengeluaran;
        this.id = id;
    }

    public LocalDate getTanggal() { return tanggal; }
    public String getKeterangan() { return keterangan; }
    public String getJenis() { return jenis; }
    public double getPemasukan() { return pemasukan; }
    public double getPengeluaran() { return pengeluaran; }
    public String getId() { return id; }
}
