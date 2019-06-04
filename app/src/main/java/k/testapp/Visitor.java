package k.testapp;

public class Visitor {
    int visit;
    byte[]  image;

    public Visitor() {
        this.visit=1;
    }

    public int getVisit() {
        return visit;
    }

    public byte[] getImage() {
        return image;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
