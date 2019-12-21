package vn.techlifegroup.wesell.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by toila on 03/16/2018.
 */

public class TransactionModel {
    public String sender;
    public String receiver;
    public String amount;
    public String senderBalance;
    public String receiverBalance;
    public String timeStamp;
    public String blockHeight;
    public String blockHash;
    public String subBlockHeight;
    public String subBlockHash;
    public String blockKey;
    public String parentBlock;
    private String agent;
    private String type;
    private String from;

    public TransactionModel() {
    }

    public TransactionModel(String sender, String receiver, String amount, String senderBalance, String receiverBalance, String timeStamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.senderBalance = senderBalance;
        this.receiverBalance = receiverBalance;
        this.timeStamp = timeStamp;
    }

    public TransactionModel(String parentBlock, String blockHash, String blockKey, String agent, String type) {
        this.parentBlock = parentBlock;
        this.blockHash = blockHash;
        this.blockKey = blockKey;
        this.agent = agent;
        this.type = type;
    }

    public TransactionModel(String amount, String timeStamp, String type, String from) {
        this.amount = amount;
        this.timeStamp = timeStamp;
        this.type = type;
        this.from = from;
    }

    public String getBlockHeight() {
        return blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAmount() {
        return amount;
    }

    public String toJSON(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("sender", getSender());
            jsonObject.put("receiver", getReceiver());
            jsonObject.put("amount", getAmount());
            jsonObject.put("sender_balance", getReceiverBalance());
            jsonObject.put("receiver_balance", getReceiverBalance());

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    public String getSenderBalance() {
        return senderBalance;
    }

    public String getReceiverBalance() {
        return receiverBalance;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setSenderBalance(String senderBalance) {
        this.senderBalance = senderBalance;
    }

    public void setReceiverBalance(String receiverBalance) {
        this.receiverBalance = receiverBalance;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSubBlockHeight() {
        return subBlockHeight;
    }

    public String getSubBlockHash() {
        return subBlockHash;
    }

    public String getBlockKey() {
        return blockKey;
    }

    public String getParentBlock() {
        return parentBlock;
    }

    public String getAgent() {
        return agent;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }
}
