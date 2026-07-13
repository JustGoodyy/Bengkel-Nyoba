package model;

public class SparePart {
    private String kode;       
    private String nama;      
    private String kategori;   
    private int stok;         
    private double hargaJual;
    private double hargaModal; 

    public SparePart() {}

    public SparePart(String kode, String nama, String kategori, int stok, double hargaJual, double hargaModal) {
        this.kode = kode;
        this.nama = nama;
        this.kategori = kategori;
        this.stok = stok;
        this.hargaJual = hargaJual;
        this.hargaModal = hargaModal;
    }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public double getHargaJual() { return hargaJual; }
    public void setHargaJual(double hargaJual) { this.hargaJual = hargaJual; }

    public double getHargaModal() { return hargaModal; }
    public void setHargaModal(double hargaModal) { this.hargaModal = hargaModal; }

    @Override
    public String toString() {
        return kode + " - " + nama + " (Stok: " + stok + ")";
    }
}
