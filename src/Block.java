import java.util.List;

public class Block {
    private List<List<Float>> values;
    private String blockType;
    private Pair<Integer, Integer> startPosition;
    private Pair<Integer, Integer> endPosition;

    public Block(List<List<Float>> values, String blockType, Pair<Integer, Integer> startPosition, Pair<Integer, Integer> endPosition) {
        this.values = values;
        this.blockType = blockType;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        String string = "Block{ blockType=" + blockType + ", startPosition=" + startPosition + ", endPosition=" + endPosition + "}\n";
        for (List<Float> list : values)
            string += list.toString() + "\n";
        return string;
    }

    public List<List<Float>> getValues() {
        return values;
    }

    public void setValues(List<List<Float>> values) {
        this.values = values;
    }

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public Pair<Integer, Integer> getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Pair<Integer, Integer> startPosition) {
        this.startPosition = startPosition;
    }

    public Pair<Integer, Integer> getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Pair<Integer, Integer> endPosition) {
        this.endPosition = endPosition;
    }
}
