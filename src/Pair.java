public class Pair<A, B> {
    private A element1;
    private B element2;

    public Pair() {
    }

    public Pair(A element1, B element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    public A getElement1() {
        return element1;
    }

    public void setElement1(A element1) {
        this.element1 = element1;
    }

    public B getElement2() {
        return element2;
    }

    public void setElement2(B element2) {
        this.element2 = element2;
    }

    @Override
    public String toString() {
        return "<" + element1 + ", " + element2 + '>';
    }
}
