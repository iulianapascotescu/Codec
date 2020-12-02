public class Main {

    public static void main(String[] args) {
        Image image = new Image("nt-P3.ppm");
        Encoder encoder = new Encoder(image);
        encoder.encode();
        Decoder decoder = new Decoder(new Image("result.ppm"), encoder);
        decoder.decode();
    }
}
