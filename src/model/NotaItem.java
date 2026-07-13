package model;

public class NotaItem {
    private String kodePart;
    private String namaPart;
    private int qty;
    private double hargaSatuan;
    private double subtotal;

    public NotaItem() {}

    public NotaItem(String kodePart, String namaPart, int qty, double hargaSatuan) {
        this.kodePart = kodePart;
        this.namaPart = namaPart;
        this.qty = qty;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = qty * hargaSatuan;
    }

    public String getKodePart() { return kodePart; }
    public void setKodePart(String kodePart) { this.kodePart = kodePart; }

    public String getNamaPart() { return namaPart; }
    public void setNamaPart(String namaPart) { this.namaPart = namaPart; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; this.subtotal = qty * hargaSatuan; }

    public double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(double hargaSatuan) { this.hargaSatuan = hargaSatuan; this.subtotal = qty * hargaSatuan; }

    public double getSubtotal() { return subtotal; }
}
