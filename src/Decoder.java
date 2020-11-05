import java.util.ArrayList;
import java.util.List;

public class Decoder {
    private Image image;
    private List<Block> Yencoder;
    private List<Block> Uencoder;
    private List<Block> Vencoder;

    public Decoder(Image image, List<Block> yencoder, List<Block> uencoder, List<Block> vencoder) {
        this.image = image;
        Yencoder = yencoder;
        Uencoder = uencoder;
        Vencoder = vencoder;
    }

    public void decode() {
        List<List<Float>> Ydecoder = new ArrayList<>();
        List<List<Float>> Udecoder = new ArrayList<>();
        List<List<Float>> Vdecoder = new ArrayList<>();
        for (int i = 0; i < Yencoder.size(); i++) {
            Block Yblock = Yencoder.get(i);
            Block Ublock = Uencoder.get(i);
            Block Vblock = Vencoder.get(i);
            Pair<Integer, Integer> startPosition = Yblock.getStartPosition();
            Pair<Integer, Integer> endPosition = Yblock.getEndPosition();
            for (int j = startPosition.getElement1(); j <= endPosition.getElement1(); j++)
                for (int k = startPosition.getElement2(); k <= endPosition.getElement2(); k++) {
                    if (Ydecoder.size() < j + 1) {
                        Ydecoder.add(new ArrayList<>());
                        Udecoder.add(new ArrayList<>());
                        Vdecoder.add(new ArrayList<>());
                    }
                    Ydecoder.get(j).add(Yblock.getValues().get(j - startPosition.getElement1()).get(k - startPosition.getElement2()));
                    Udecoder.get(j).add(Ublock.getValues().get((j - startPosition.getElement1()) / 2).get((k - startPosition.getElement2()) / 2));
                    Vdecoder.get(j).add(Vblock.getValues().get((j - startPosition.getElement1()) / 2).get((k - startPosition.getElement2()) / 2));
                }
        }
        this.image.setYmatrix(Ydecoder);
        this.image.setUmatrix(Udecoder);
        this.image.setVmatrix(Vdecoder);
        this.image.setResolution(new Pair<>(Ydecoder.get(0).size(), Ydecoder.size()));
        image.YUVtoRGB();
        image.writeToFile();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Block> getYencoder() {
        return Yencoder;
    }

    public void setYencoder(List<Block> yencoder) {
        Yencoder = yencoder;
    }

    public List<Block> getUencoder() {
        return Uencoder;
    }

    public void setUencoder(List<Block> uencoder) {
        Uencoder = uencoder;
    }

    public List<Block> getVencoder() {
        return Vencoder;
    }

    public void setVencoder(List<Block> vencoder) {
        Vencoder = vencoder;
    }
}
