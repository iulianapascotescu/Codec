import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Encoder {
    private Image image;
    private List<Block> Yencoder;
    private List<Block> Uencoder;
    private List<Block> Vencoder;
    private List<Block> Gencoder;
    private List<List<Integer>> quantizationMatrix;
    private List<List<Integer>> vectorOfBytes;

    public Encoder(Image image) {
        this.image = image;
        this.Yencoder = new ArrayList<>();
        this.Uencoder = new ArrayList<>();
        this.Vencoder = new ArrayList<>();
        this.Gencoder = new ArrayList<>();
        this.vectorOfBytes = new ArrayList<>();
        this.quantizationMatrix = new ArrayList<>();
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 4, 4, 6, 10, 16, 20, 24)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(5, 5, 6, 8, 10, 23, 24, 22)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 5, 6, 10, 16, 23, 28, 22)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(6, 7, 9, 12, 20, 35, 32, 25)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(7, 9, 15, 22, 27, 44, 41, 31)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(10, 14, 22, 26, 32, 42, 45, 37)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(20, 26, 31, 35, 41, 48, 48, 40)));
        quantizationMatrix.add(new ArrayList<>(Arrays.asList(29, 37, 38, 39, 45, 40, 41, 40)));
    }

    public void encode() {
        //lab1
        this.image.readFromFile();
        this.image.RGBtoYUV();
        this.formBlocksYUV();

        //lab2
        for (int i = 0; i < Uencoder.size(); i++) {
            Uencoder.get(i).setValues(convertBlock4x4to8x8(Uencoder.get(i).getValues()));
            Vencoder.get(i).setValues(convertBlock4x4to8x8(Vencoder.get(i).getValues()));
        }
        for (int i = 0; i < Uencoder.size(); i++) {
            Yencoder.get(i).setValues(subtract128(Yencoder.get(i).getValues()));
            Uencoder.get(i).setValues(subtract128(Uencoder.get(i).getValues()));
            Vencoder.get(i).setValues(subtract128(Vencoder.get(i).getValues()));
        }
        for (int i = 0; i < Uencoder.size(); i++) {
            Gencoder.add(new Block(forwardDCT(Yencoder.get(i).getValues()), Yencoder.get(i).getBlockType(), Yencoder.get(i).getStartPosition(), Yencoder.get(i).getEndPosition()));
            Gencoder.add(new Block(forwardDCT(Uencoder.get(i).getValues()), Uencoder.get(i).getBlockType(), Uencoder.get(i).getStartPosition(), Uencoder.get(i).getEndPosition()));
            Gencoder.add(new Block(forwardDCT(Vencoder.get(i).getValues()), Vencoder.get(i).getBlockType(), Vencoder.get(i).getStartPosition(), Vencoder.get(i).getEndPosition()));
        }

        for (int i = 0; i < Gencoder.size(); i++) {
            this.Gencoder.get(i).setValues(this.quantizationPhase(Gencoder.get(i).getValues()));
        }

        //lab3
        this.entropyEncoding();
    }

    private void entropyEncoding() {
        for (int i = 0; i < Gencoder.size(); i++) {
            List<Float> zigZagArray = zigZagParsing(Gencoder.get(i).getValues());
            int runlength = 0, amplitude, size;

            //DC coefficient
            amplitude = Math.round(zigZagArray.get(0));
            size = getSize(amplitude);
            this.vectorOfBytes.add(new ArrayList<>(Arrays.asList(size, amplitude)));

            for (int j = 1; j < zigZagArray.size(); j++) {
                if (zigZagArray.get(j) != 0f) {
                    amplitude = Math.round(zigZagArray.get(j));
                    size = getSize(amplitude);
                    this.vectorOfBytes.add(new ArrayList<>(Arrays.asList(runlength, size, amplitude)));
                    runlength = 0;
                } else runlength++;
            }
            if(runlength>0)
                this.vectorOfBytes.add(new ArrayList<>(Arrays.asList(0, 0)));
        }
    }

    public int getSize(int value) {
        value = Math.abs(value);
        int k = 1, n = 0;
        while (value >= k) {
            k *= 2;
            n++;
        }
        return n;
    }

    public List<Float> zigZagParsing(List<List<Float>> matrix) {
        List<Float> result = new ArrayList<>();
        int i = 0, j = 0;
        boolean up = true;
        while (i <= 7 && j <= 7) {
            if (up) {
                result.add(matrix.get(i).get(j));
                if (j != 7)
                    j++;
                else
                    i++;
                result.add(matrix.get(i++).get(j--));
                while (j > 0 && i < 7)
                    result.add(matrix.get(i++).get(j--));
                up = false;
            } else {
                result.add(matrix.get(i).get(j));
                if (i != 7)
                    i++;
                else
                    j++;
                result.add(matrix.get(i--).get(j++));
                while (j < 7 && i > 0)
                    result.add(matrix.get(i--).get(j++));
                up = true;
            }
        }
        return result;
    }

    public void formBlocksYUV() {
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

    public List<List<Float>> convertBlock4x4to8x8(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            resultMatrix.add(new ArrayList<>());
            for (int j = 0; j < 8; j++) {
                resultMatrix.get(i).add(matrix.get(i / 2).get(j / 2));
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> subtract128(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            resultMatrix.add(new ArrayList<>());
            for (int j = 0; j < 8; j++) {
                resultMatrix.get(i).add(matrix.get(i).get(j) - 128);
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> forwardDCT(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int u = 0; u < 8; u++) {
            resultMatrix.add(new ArrayList<>());
            for (int v = 0; v < 8; v++) {
                float value = 0.25f;
                if (u == 0 && v == 0) {
                    value *= (float) (1 / Math.sqrt(2)) * (1 / Math.sqrt(2));
                } else if (u == 0 || v == 0) {
                    value *= (float) (1 / Math.sqrt(2));
                }
                float sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += matrix.get(x).get(y) * Math.cos((2 * x + 1) * u * Math.PI / 16.0f) * Math.cos((2 * y + 1) * v * Math.PI / 16.0f);
                    }
                }
                value *= sum;
                resultMatrix.get(u).add(value);
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> quantizationPhase(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            resultMatrix.add(new ArrayList<>());
            for (int j = 0; j < 8; j++) {
                resultMatrix.get(i).add((float) (int) (matrix.get(i).get(j) / this.quantizationMatrix.get(i).get(j)));
            }
        }
        return resultMatrix;
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

    public List<Block> getGencoder() {
        return Gencoder;
    }

    public List<List<Integer>> getVectorOfBytes() {
        return this.vectorOfBytes;
    }

    public Image getImage(){
        return this.image;
    }
}
