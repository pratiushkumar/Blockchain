import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    public String Transactionid;
    public PublicKey Senderaddr;
    public PublicKey receiveraddr;
    public float value;
    public byte[] Signature; // to Prevent someone else from taking signatures

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> output = new ArrayList<TransactionOutput>();

            public Transaction(PublicKey from, PublicKey to ,float value,ArrayList<TransactionInput> inputs) {
                this.Senderaddr = from;
                this.receiveraddr =to;
                this.value = value;
                this.inputs = inputs;
            }
     //calculate transaction hash (which will beused asits id
    String calculateHsh() {
                sequence++;//increase the sequence to avoid 2 identical transaction having same identical value
        return Util.applySha256(
                Util.getStringFromkey(Senderaddr) + Util.getStringFromKey(receiveraddr) +Float.toString(value) +sequence

        )
    }


}
