public class Main {

    public static void main(String[] args) {
        Image image = new Image("nt-P3.ppm");
        Encoder encoder = new Encoder(image);
        encoder.encode();
        //System.out.println(encoder.getYencoder());
        //System.out.println(encoder.getUencoder());
        //System.out.println(encoder.getVencoder());
        Decoder decoder = new Decoder(new Image("result.ppm"), encoder.getYencoder(), encoder.getUencoder(), encoder.getVencoder());
        decoder.decode();
    }
}
