import java.security.*;

public class Wallet
 {
      //CREATING A WALLET

     public PrivateKey pvt;
     public PublicKey pub;

     public Wallet() {
         generateBothKey();
     }
     public void generateBothKey() {
         try {
             KeyPairGenerator KeyGen=   KeyPairGenerator.getInstance("RSA") ;
             SecureRandom random =  SecureRandom.getInstance("Windows-PRNG");
             KeyGen.initialize(2048,random);

             KeyPair kp = KeyGen.generateKeyPair();
             //set public and private key from the keypair
             pvt =kp.getPrivate();
             pub = kp.getPublic();
         }
         catch(Exception  e ) {
             throw new RuntimeException();
         }
     }

 }

