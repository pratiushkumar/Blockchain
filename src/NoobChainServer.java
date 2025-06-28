

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;

public class NoobChainServer {
    private static NoobChain noobChain = new NoobChain();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        // Pre-initialize NoobChain with wallets and genesis block
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        noobChain.walletA = new Wallet();
        noobChain.walletB = new Wallet();
        noobChain.coinbase = new Wallet();
        noobChain.genesisTransaction = new Transaction(noobChain.coinbase.publicKey, noobChain.walletA.publicKey, 100f, null);
        noobChain.genesisTransaction.generateSignature(noobChain.coinbase.privateKey);
        noobChain.genesisTransaction.transactionId = "0";
        noobChain.genesisTransaction.outputs.add(new TransactionOutput(noobChain.genesisTransaction.reciepient, noobChain.genesisTransaction.value, noobChain.genesisTransaction.transactionId));
        noobChain.UTXOs.put(noobChain.genesisTransaction.outputs.get(0).id, noobChain.genesisTransaction.outputs.get(0));
        Block genesis = new Block("0");
        genesis.addTransaction(noobChain.genesisTransaction);
        noobChain.addBlock(genesis);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/chain", NoobChainServer::handleGetChain);
        server.createContext("/api/transaction", NoobChainServer::handleAddTransaction);
        server.createContext("/api/mine", NoobChainServer::handleMineBlock);
        server.createContext("/api/isValid", NoobChainServer::handleIsValid);
        server.createContext("/api/wallets", NoobChainServer::handleGetWallets);
        server.createContext("/api/send", NoobChainServer::handleSendFunds);
        server.createContext("/", NoobChainServer::handleStaticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:8080");
    }

    private static void handleGetChain(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        JsonArray chainArray = new JsonArray();
        for (Block block : NoobChain.blockchain) {
            JsonObject blockJson = new JsonObject();
            blockJson.addProperty("hash", block.hash);
            blockJson.addProperty("previousHash", block.previousHash);
            blockJson.addProperty("merkleRoot", block.merkleRoot);
            blockJson.addProperty("timeStamp", block.timeStamp);
            blockJson.addProperty("nonce", block.nonce);
            JsonArray transactionsArray = new JsonArray();
            for (Transaction tx : block.transactions) {
                JsonObject txJson = new JsonObject();
                txJson.addProperty("transactionId", tx.transactionId);
                txJson.addProperty("sender", StringUtil.getStringFromKey(tx.sender));
                txJson.addProperty("recipient", StringUtil.getStringFromKey(tx.reciepient));
                txJson.addProperty("value", tx.value);
                transactionsArray.add(txJson);
            }
            blockJson.add("transactions", transactionsArray);
            chainArray.add(blockJson);
        }
        os.write(gson.toJson(chainArray).getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static void handleAddTransaction(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson.fromJson(body, JsonObject.class);
        String senderPubKey = json.get("senderPubKey").getAsString();
        String recipientPubKey = json.get("recipientPubKey").getAsString();
        float value = json.get("value").getAsFloat();
        PublicKey senderKey = StringUtil.getKeyFromString(senderPubKey);
        PublicKey recipientKey = StringUtil.getKeyFromString(recipientPubKey);
        Transaction tx = new Transaction(senderKey, recipientKey, value, new ArrayList<>());
        tx.generateSignature(NoobChain.walletA.privateKey); // Simplified; use actual sender's private key in production
        tx.transactionId = tx.calulateHash();
        Block currentBlock = NoobChain.blockchain.get(NoobChain.blockchain.size() - 1);
        if (currentBlock.addTransaction(tx)) {
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            JsonObject response = new JsonObject();
            response.addProperty("message", "Transaction added");
            os.write(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
            os.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
            OutputStream os = exchange.getResponseBody();
            JsonObject response = new JsonObject();
            response.addProperty("message", "Transaction failed");
            os.write(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    private static void handleMineBlock(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        Block newBlock = new Block(NoobChain.blockchain.get(NoobChain.blockchain.size() - 1).hash);
        NoobChain.addBlock(newBlock);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        JsonObject blockJson = new JsonObject();
        blockJson.addProperty("hash", newBlock.hash);
        blockJson.addProperty("previousHash", newBlock.previousHash);
        os.write(gson.toJson(blockJson).getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static void handleIsValid(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        JsonObject response = new JsonObject();
        response.addProperty("isValid", NoobChain.isChainValid());
        os.write(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static void handleGetWallets(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        JsonObject walletsJson = new JsonObject();
        walletsJson.addProperty("walletA", StringUtil.getStringFromKey(NoobChain.walletA.publicKey));
        walletsJson.addProperty("walletB", StringUtil.getStringFromKey(NoobChain.walletB.publicKey));
        walletsJson.addProperty("coinbase", StringUtil.getStringFromKey(NoobChain.coinbase.publicKey));
        os.write(gson.toJson(walletsJson).getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static void handleSendFunds(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject json = gson.fromJson(body, JsonObject.class);
        String recipientPubKey = json.get("recipientPubKey").getAsString();
        float value = json.get("value").getAsFloat();
        Transaction tx = NoobChain.walletA.sendFunds(StringUtil.getKeyFromString(recipientPubKey), value);
        if (tx != null && tx.processTransaction()) {
            Block currentBlock = new Block(NoobChain.blockchain.get(NoobChain.blockchain.size() - 1).hash);
            currentBlock.addTransaction(tx);
            NoobChain.addBlock(currentBlock);
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            JsonObject response = new JsonObject();
            response.addProperty("message", "Funds sent");
            os.write(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
            os.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
            OutputStream os = exchange.getResponseBody();
            JsonObject response = new JsonObject();
            response.addProperty("message", "Failed to send funds");
            os.write(gson.toJson(response).getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    private static void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        File file = new File("src/main/resources/static" + path);
        if (file.exists()) {
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();
            FileInputStream fis = new FileInputStream(file);
            fis.transferTo(os);
            fis.close();
            os.close();
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private static PublicKey getKeyFromString(String keyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            return keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
