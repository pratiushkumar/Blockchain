import java.util.Date;

public class Block {
    public String  hash;
    public String Previoushash;
    private String data;
    private long timeStamp;
    private int nonce;

    Block(String Previoushash,String imgcode) {
        this.Previoushash = Previoushash;
        this.data = imgcode;
        this.timeStamp = new Date().getTime();
        this.hash =CalculateHash();
    }
//must calculate the hash from all the parts of the blocjk wwe dont want to bee tampered
    //so fro block we will include previoushash,data and timestamp

    public String CalculateHash() {
        String calculatedhash  = Util.applysha256(Previoushash+Long.toString(timeStamp)+Integer.toString(nonce)+data);
        return calculatedhash;
    }
    public void mining(int difficulty) {
        String target  = new String (new char[difficulty]).replace('\0','0');
        //create a string with difficulty *"0"
        while(!hash.substring(0,difficulty).equals(target)) {
            nonce++;
            hash =CalculateHash();
        }
        System.out.println("Block Mined!!!" +hash);
    }

}
