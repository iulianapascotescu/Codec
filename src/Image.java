import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Image {
    private String filename;
    private String format;
    private Pair<Integer, Integer> resolution;
    private int maximumValue;
    private List<List<Integer>> Rmatrix;
    private List<List<Integer>> Gmatrix;
    private List<List<Integer>> Bmatrix;
    private List<List<Float>> Ymatrix;
    private List<List<Float>> Umatrix;
    private List<List<Float>> Vmatrix;

    public Image() {
    }

    public Image(String filename) {
        this.filename = filename;
        this.Rmatrix = new ArrayList<>();
        this.Gmatrix = new ArrayList<>();
        this.Bmatrix = new ArrayList<>();
        this.Ymatrix = new ArrayList<>();
        this.Umatrix = new ArrayList<>();
        this.Vmatrix = new ArrayList<>();
    }

    public void readFromFile() {
        try {
            File file = new File(this.filename);
            Scanner reader = new Scanner(file);
            this.format = reader.nextLine();
            reader.nextLine();
            this.resolution = new Pair<>(reader.nextInt(), reader.nextInt());
            this.maximumValue = reader.nextInt();
            int columns = resolution.getElement1();
            int j = 0;
            List<Integer> Rlist = new ArrayList<>();
            List<Integer> Glist = new ArrayList<>();
            List<Integer> Blist = new ArrayList<>();
            while (reader.hasNextInt()) {
                Rlist.add(reader.nextInt());
                Glist.add(reader.nextInt());
                Blist.add(reader.nextInt());
                j++;
                if (j == columns) {
                    j = 0;
                    Rmatrix.add(Rlist);
                    Gmatrix.add(Glist);
                    Bmatrix.add(Blist);
                    Rlist = new ArrayList<>();
                    Glist = new ArrayList<>();
                    Blist = new ArrayList<>();
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeToFile() {
        try {
            File file = new File(this.filename);
            FileWriter writer = new FileWriter(file);
            writer.write("P3\n");
            writer.write("# CREATOR: GIMP PNM Filter Version 1.1\n");
            writer.write(this.resolution.getElement1().toString() + " " + this.resolution.getElement2().toString() + "\n");
            writer.write("255\n");
            for (int i = 0; i < this.resolution.getElement2(); i++)
                for (int j = 0; j < this.resolution.getElement1(); j++) {
                    writer.write(this.Rmatrix.get(i).get(j).toString() + "\n");
                    writer.write(this.Gmatrix.get(i).get(j).toString() + "\n");
                    writer.write(this.Bmatrix.get(i).get(j).toString() + "\n");
                }
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void RGBtoYUV() {
        List<Float> Ylist;
        List<Float> Ulist;
        List<Float> Vlist;
        for (int i = 0; i < this.resolution.getElement2(); i++) {
            Ylist = new ArrayList<>();
            Ulist = new ArrayList<>();
            Vlist = new ArrayList<>();
            for (int j = 0; j < this.resolution.getElement1(); j++) {
                int R = this.getRmatrix().get(i).get(j);
                int G = this.getGmatrix().get(i).get(j);
                int B = this.getBmatrix().get(i).get(j);
                float Y = (float) (0.299 * R + 0.587 * G + 0.114 * B);
                float U = (float) (128 - 0.1687 * R - 0.3312 * G + 0.5 * B);
                float V = (float) (128 + 0.5 * R - 0.4186 * G - 0.0813 * B);
                Ylist.add(Y);
                Ulist.add(U);
                Vlist.add(V);
            }
            Ymatrix.add(Ylist);
            Umatrix.add(Ulist);
            Vmatrix.add(Vlist);
        }
    }

    public void YUVtoRGB() {
        List<Integer> Rlist;
        List<Integer> Glist;
        List<Integer> Blist;
        for (int i = 0; i < this.resolution.getElement2(); i++) {
            Rlist = new ArrayList<>();
            Glist = new ArrayList<>();
            Blist = new ArrayList<>();

            for (int j = 0; j < this.resolution.getElement1(); j++) {
                float Y = this.getYmatrix().get(i).get(j);
                float U = this.getUmatrix().get(i).get(j);
                float V = this.getVmatrix().get(i).get(j);

                int R = (int) (Y + 1.402 * (V - 128));
                int G = (int) (Y - 0.344 * (U - 128) - 0.714 * (V - 128));
                int B = (int) (Y + 1.772 * (U - 128));

                if (R > 255) R = 255;
                if (G > 255) G = 255;
                if (B > 255) B = 255;

                if (R < 0) R = 0;
                if (G < 0) G = 0;
                if (B < 0) B = 0;

                Rlist.add(R);
                Glist.add(G);
                Blist.add(B);
            }
            Rmatrix.add(Rlist);
            Gmatrix.add(Glist);
            Bmatrix.add(Blist);
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Pair<Integer, Integer> getResolution() {
        return resolution;
    }

    public void setResolution(Pair<Integer, Integer> resolution) {
        this.resolution = resolution;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    public List<List<Integer>> getRmatrix() {
        return Rmatrix;
    }

    public void setRmatrix(List<List<Integer>> rmatrix) {
        Rmatrix = rmatrix;
    }

    public List<List<Integer>> getGmatrix() {
        return Gmatrix;
    }

    public void setGmatrix(List<List<Integer>> gmatrix) {
        Gmatrix = gmatrix;
    }

    public List<List<Integer>> getBmatrix() {
        return Bmatrix;
    }

    public void setBmatrix(List<List<Integer>> bmatrix) {
        Bmatrix = bmatrix;
    }

    public List<List<Float>> getYmatrix() {
        return Ymatrix;
    }

    public void setYmatrix(List<List<Float>> ymatrix) {
        Ymatrix = ymatrix;
    }

    public List<List<Float>> getUmatrix() {
        return Umatrix;
    }

    public void setUmatrix(List<List<Float>> umatrix) {
        Umatrix = umatrix;
    }

    public List<List<Float>> getVmatrix() {
        return Vmatrix;
    }

    public void setVmatrix(List<List<Float>> vmatrix) {
        Vmatrix = vmatrix;
    }
}
