package cr.ac.itcr.zsnails.pureharvest;

public interface Stateful<T> {
    T getState();

    void setState(T state);
}
