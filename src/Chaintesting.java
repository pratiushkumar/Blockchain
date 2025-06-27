import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class Chaintesting {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;

    public static void main(String[] args) {
        Block genesisBlock = new Block("0", "This is the first block ");
        System.out.println("Hash for block 1 :" + genesisBlock.hash);
        blockchain.add(genesisBlock); // Add genesis block first
        blockchain.get(0).mining(difficulty);

        Block secondBlock = new Block(genesisBlock.hash, "hey this the second block ");
        System.out.println("hash for second block or block 2" + secondBlock.hash);
        blockchain.add(secondBlock); // Add second block
        blockchain.get(1).mining(difficulty);

        Block thirdblock = new Block(secondBlock.hash, "this is the block ");
        System.out.println("hash for block 3 " + thirdblock.hash);
        blockchain.add(thirdblock); // Add third block
        blockchain.get(2).mining(difficulty);

        blockchain.add(new Block("Hi i am the first and genesis block ", "0"));
        blockchain.get(0).mining(difficulty);
        blockchain.add(new Block("Yo i am the second block", blockchain.get(blockchain.size() - 1).hash));
        blockchain.get(1).mining(difficulty);
        blockchain.add(new Block("this is third block ", blockchain.get(blockchain.size() - 1).hash));
        blockchain.get(2).mining(difficulty);

        System.out.println("\nBlockchain is valid");
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\n The block chain :");
        System.out.println(blockchainJson);
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        //check for registered hash and calculated hash
        //loop through blockchain to check hashes
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            //compare registered hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.CalculateHash())) {
                System.out.println("Current hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.Previoushash)) {
                System.out.println("Previous hash is not equal ");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("this block has not been mined ");
                return false;
            }
        }

        return true;
    }
}