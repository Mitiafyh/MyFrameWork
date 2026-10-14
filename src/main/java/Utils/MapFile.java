package Utils;

public class MapFile {
    String nom;
    String extension;
    Byte[] bytes;

    public MapFile(String nom, String extension, Byte[] bytes) {
        this.nom = nom;
        this.extension = extension;
        this.bytes = bytes;
    }

    public MapFile(){

    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Byte[] getBytes() {
        return bytes;
    }

    public void setBytes(Byte[] bytes) {
        this.bytes = bytes;
    }
    
}
