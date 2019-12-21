package vn.techlifegroup.wesell.model;

import com.google.firebase.database.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import vn.techlifegroup.wesell.utils.Utils;


/**
 * Created by toila on 03/20/2018.
 */

public class SubBlock {
    int index;
    String previousHash;
    TransactionModel transaction;
    String hash;
    String timeStamp;
    String name;
    String txTotal;

    public SubBlock() {
    }

    public SubBlock(int index, String previousHash, TransactionModel transaction, String hash, String timeStamp) {
        this.index = index;
        this.previousHash = previousHash;
        this.transaction = transaction;
        this.hash = calculateHash();
        this.timeStamp = timeStamp;
    }

    public SubBlock(String previousHash, TransactionModel transaction) {
        this.previousHash = previousHash;
        this.transaction = transaction;
    }

    public String calculateHash(){
        Gson gson = new GsonBuilder().create();
        //JsonArray transactionArray = gson.toJsonTree(this.transactions).getAsJsonArray();
        return Utils.applySha256((this.previousHash + gson.toJson(this.transaction)));
    }

    public int getIndex() {
        return index;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public TransactionModel getTransaction() {
        return transaction;
    }

    public String getHash() {
        return hash;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getName() {
        return name;
    }

    public String getTxTotal() {
        return txTotal;
    }
}
