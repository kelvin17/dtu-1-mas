package searchclient.cbs.model;

import java.io.*;

public interface AbstractDeepCopy<T> extends Serializable {

    default T deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.printf("Deep copy failed + Obj[%s]\n", this.toString());
            throw new RuntimeException(e);
        }
    }
}
