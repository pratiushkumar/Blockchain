import java.util.ArrayList;
import com.google.gson.GsonBuilder;
public class Chaintesting {
    public static  ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;
    public static void main(String[]  args) {
        Block genesisiBlock = new Block("0", "This is the first block ");
        System.out.println("HAsh for block 1 :" + genesisiBlock.hash);

        Block secondBlock = new Block("hey this the second block ", genesisiBlock.hash);
        System.out.println("hash for second block or block 2" + secondBlock.hash);

        Block thirdblock = new Block("this is the block ", secondBlock.hash);
        System.out.println("hash for block 3 " + thirdblock.hash);


        blockchain.add(new Block("Hi i am the first and genesis block ", "0"));
        blockchain.get(0).mining(difficulty);
        blockchain.add(new Block("Yo i am the second block", blockchain.get(blockchain.size() - 1).hash));
        blockchain.get(1).mining(difficulty);
        blockchain.add(new Block("this is third block ", blockchain.get(blockchain.size() - 1).hash));
        blockchain.get(2).mining(difficulty);
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println(blockchainJson);
    }
public static Boolean isChainValid() {
            Block currentBlock;
            Block previousBlock;
            //check for rtegistered hash and calculated hash
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
                    System.out.println("Previous hash uis not equal ");
                    return false;
                }
            }

       return true;
    }

    }

