import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder {
    private Image image;
    private List<Block> Yencoder;
    private List<Block> Uencoder;
    private List<Block> Vencoder;
    private List<Block> Gencoder;
    private List<List<Integer>> quantizationMatrix;
    private List<List<Integer>> vectorOfBytes;
    private Encoder encoder;

    public Decoder(Image image, Encoder encoder) {
        this.encoder = encoder;
        this.image = image;
        this.vectorOfBytes = encoder.getVectorOfBytes();
        this.Gencoder = new ArrayList<>();
        Yencoder = new ArrayList<>();
        Uencoder = new ArrayList<>();
        Vencoder = new ArrayList<>();
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

    public void decode() {
        //lab3
        this.entropyDecoding();

        //lab2
        for (int i = 0; i < Gencoder.size(); i++)
            Gencoder.get(i).setValues(this.dequantizationPhase(Gencoder.get(i).getValues()));

        for (int i = 0; i < Gencoder.size(); i++) {
            Block newBlock = new Block(this.inverseDCT(Gencoder.get(i).getValues()), Gencoder.get(i).getBlockType(), Gencoder.get(i).getStartPosition(), Gencoder.get(i).getEndPosition());
            switch (Gencoder.get(i).getBlockType()) {
                case "Y" -> this.Yencoder.add(newBlock);
                case "U" -> this.Uencoder.add(newBlock);
                case "V" -> this.Vencoder.add(newBlock);
            }
        }

        for (int i = 0; i < Yencoder.size(); i++) {
            Yencoder.get(i).setValues(add128(Yencoder.get(i).getValues()));
            Uencoder.get(i).setValues(add128(Uencoder.get(i).getValues()));
            Vencoder.get(i).setValues(add128(Vencoder.get(i).getValues()));
        }

        for (int i = 0; i < Vencoder.size(); i++) {
            Uencoder.get(i).setValues(this.convertBlock8to4(Uencoder.get(i).getValues()));
            Vencoder.get(i).setValues(this.convertBlock8to4(Vencoder.get(i).getValues()));
        }

        //lab1
        this.conversion();

    }

    public void entropyDecoding(){
        int index = 0;
        String type = "Y";

        int imageWidth = this.encoder.getImage().getResolution().getElement1();
        int blockLine = 0;
        int blockColumn = 0;

        while (index < vectorOfBytes.size()) {
            List<Float> values = new ArrayList<>();

            List<Integer> DCcoeff = vectorOfBytes.get(index);
            index++;
            values.add(Float.parseFloat(DCcoeff.get(1).toString()));
            while (values.size() < 64 && !(vectorOfBytes.get(index).size() == 2 && vectorOfBytes.get(index).get(0) == 0 && vectorOfBytes.get(index).get(1) == 0)) {
                for (int k = 0; k < vectorOfBytes.get(index).get(0); k++)
                    values.add(0f);
                values.add(Float.valueOf(vectorOfBytes.get(index).get(2)));
                index++;
            }

            if (values.size() < 64) {
                index++;
                for (int k = values.size(); k < 64; k++)
                    values.add(0f);
            }

            List<List<Float>> fromZigZagValues = transformFromZigZag(values);
            this.Gencoder.add(new Block(fromZigZagValues, type, new Pair<>(blockLine, blockColumn), new Pair<>(blockLine + 7, blockColumn + 7)));

            switch (type) {
                case "Y" -> type = "U";
                case "U" -> type = "V";
                case "V" -> {
                    type = "Y";
                    blockColumn += 8;
                    if (blockColumn == imageWidth) {
                        blockColumn = 0;
                        blockLine += 8;
                    }
                }
            }
        }
    }

    public List<List<Float>> transformFromZigZag(List<Float> array) {
        List<List<Float>> result = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            result.add(new ArrayList<>());
        int i = 0, j = 0, index = 0;
        boolean isLine = true;
        while (i <= 7 && j <= 7) {
            if (isLine) {
                result.get(i).add(array.get(index++));
                if (j != 7)
                    j++;
                else
                    i++;
                result.get(i).add(array.get(index++));
                i++;
                j--;
                while (j > 0 && i < 7) {
                    result.get(i).add(array.get(index++));
                    j--;
                    i++;
                }
                isLine = false;
            } else {
                result.get(i).add(array.get(index++));
                if (i != 7)
                    i++;
                else
                    j++;
                result.get(i).add(array.get(index++));
                i--;
                j++;
                while (j < 7 && i > 0) {
                    result.get(i).add(array.get(index++));
                    i--;
                    j++;
                }
                isLine = true;
            }
        }
        return result;
    }

    public List<List<Float>> dequantizationPhase(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            resultMatrix.add(new ArrayList<>());
            for (int j = 0; j < 8; j++) {
                resultMatrix.get(i).add(matrix.get(i).get(j) * this.quantizationMatrix.get(i).get(j));
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> inverseDCT(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            resultMatrix.add(new ArrayList<>());
            for (int y = 0; y < 8; y++) {
                float sum = 0;
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        double cos = Math.cos((2 * x + 1) * u * Math.PI / 16.0);
                        double cos1 = Math.cos((2 * y + 1) * v * Math.PI / 16.0);
                        if (u == 0 && v == 0)
                            sum += (1.0 / Math.sqrt(2)) * (1.0 / Math.sqrt(2)) * matrix.get(u).get(v) * cos * cos1;
                        else if (u == 0 || v == 0)
                            sum += (1.0 / Math.sqrt(2)) * matrix.get(u).get(v) * cos * cos1;
                        else
                            sum += matrix.get(u).get(v) * cos * cos1;
                    }
                }
                sum *= 0.25f;
                resultMatrix.get(x).add(sum);
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> add128(List<List<Float>> matrix) {
        List<List<Float>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            resultMatrix.add(new ArrayList<>());
            for (int j = 0; j < 8; j++) {
                resultMatrix.get(i).add(matrix.get(i).get(j) + 128);
            }
        }
        return resultMatrix;
    }

    public List<List<Float>> convertBlock8to4(List<List<Float>> matrix) {
        List<List<Float>> matrixResult = new ArrayList<>();
        for (int i = 0; i < 8; i = i + 2) {
            matrixResult.add(new ArrayList<>());
            for (int j = 0; j < 8; j = j + 2)
                matrixResult.get(i / 2).add(matrix.get(i).get(j));
        }
        return matrixResult;
    }

    public void conversion() {
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
