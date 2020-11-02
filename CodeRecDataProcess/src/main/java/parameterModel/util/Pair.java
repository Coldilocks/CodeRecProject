package parameterModel.util;

public class Pair<A,B> {

    // final so that we don't need getters
    public A a;
    public B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Pair){
            Pair p = (Pair)o;
            if ((a == null) && (p.a != null)){
                return false;
            }
            if ((b == null) && (p.b != null)){
                return false;
            }
            if (!a.equals(p.a)){
                return false;
            }
            if (!b.equals(p.b)){
                return false;
            }

            return true;

        } else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        int h1 = (a == null) ? 0 : a.hashCode();
        int h2 = (b == null) ? 0 : b.hashCode();

        return h1 ^ h2;
    }
}
