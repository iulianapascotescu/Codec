import java.util.ArrayList;
import java.util.List;

public class Encoder {
    private Image image;
    private List<Block> Yencoder;
    private List<Block> Uencoder;
    private List<Block> Vencoder;

    public Encoder(Image image) {
        this.image = image;
        this.Yencoder = new ArrayList<>();
        this.Uencoder = new ArrayList<>();
        this.Vencoder = new ArrayList<>();
    }

    public void encode() {
        this.image.readFromFile();
        this.image.RGBtoYUV();
        List<List<Float>> Ymatrix = this.image.getYmatrix();
        List<List<Float>> Umatrix = this.image.getUmatrix();
        List<List<Float>> Vmatrix = this.image.getVmatrix();

        int blockLine = 0, blockColumn = 0;
        while (blockLine < this.image.getResolution().getElement2() / 8) {
            while (blockColumn < this.image.getResolution().getElement1() / 8) {
                List<List<Float>> YblockList = new ArrayList<>();
                List<List<Float>> UblockList = new ArrayList<>();
                List<List<Float>> VblockList = new ArrayList<>();
                float Usum = 0, Vsum = 0;
                for (int i = blockLine * 8; i < (blockLine + 1) * 8; i++) {
                    List<Float> Ylist = new ArrayList<>();
                    List<Float> Ulist = new ArrayList<>();
                    List<Float> Vlist = new ArrayList<>();
                    for (int j = blockColumn * 8; j < (blockColumn + 1) * 8; j++) {
                        Ylist.add(Ymatrix.get(i).get(j));
                        if (i % 2 == 0) {
                            Usum += Umatrix.get(i).get(j) + Umatrix.get(i + 1).get(j);
                            Vsum += Vmatrix.get(i).get(j) + Vmatrix.get(i + 1).get(j);
                            if (j % 2 == 1) {
                                Ulist.add(Usum / 4);
                                Vlist.add(Vsum / 4);
                                Usum = 0;
                                Vsum = 0;
                            }
                        }
                    }
                    YblockList.add(Ylist);
                    if (i % 2 == 0) {
                        UblockList.add(Ulist);
                        VblockList.add(Vlist);
                    }
                }
                Yencoder.add(new Block(YblockList, "Y", new Pair<>(blockLine * 8, blockColumn * 8), new Pair<>((blockLine + 1) * 8 - 1, (blockColumn + 1) * 8 - 1)));
                Uencoder.add(new Block(UblockList, "U", new Pair<>(blockLine * 8, blockColumn * 8), new Pair<>((blockLine + 1) * 8 - 1, (blockColumn + 1) * 8 - 1)));
                Vencoder.add(new Block(VblockList, "V", new Pair<>(blockLine * 8, blockColumn * 8), new Pair<>((blockLine + 1) * 8 - 1, (blockColumn + 1) * 8 - 1)));
                blockColumn++;
            }
            blockLine++;
            blockColumn = 0;
        }
    }

    public List<Block> getYencoder() {
        return Yencoder;
    }

    public List<Block> getUencoder() {
        return Uencoder;
    }

    public List<Block> getVencoder() {
        return Vencoder;
    }
}
