package vn.techlifegroup.wesell.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Promotion {
    String promotionName;
    String promotionStartDate;
    String promotionEndDate;
    String orderDiscount;
    String promotionType;
    String promotionProductDiscount;
    String promotionProductDiscountName;
    String promotionBuyName;
    String promotionGetName;
    String promotionBuyQuantity;
    String promotionGetQuantity;
    String promotionGetCode;
    String promotionCode;
    String startDate;
    String endDate;
    String programName;
    String content;

    public Promotion() {
    }

    public Promotion(String promotionName, String promotionCode) {
        this.promotionName = promotionName;
        this.promotionCode = promotionCode;
    }

    public Promotion(String promotionName, String promotionStartDate, String promotionEndDate) {
        this.promotionName = promotionName;
        this.promotionStartDate = promotionStartDate;
        this.promotionEndDate = promotionEndDate;
    }

    public Promotion(String promotionBuyName, String promotionGetName, String promotionBuyQuantity, String promotionGetQuantity,String promotionGetCode) {
        this.promotionBuyName = promotionBuyName;
        this.promotionGetName = promotionGetName;
        this.promotionBuyQuantity = promotionBuyQuantity;
        this.promotionGetQuantity = promotionGetQuantity;
        this.promotionGetCode = promotionGetCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getProgramName() {
        return programName;
    }

    public String getContent() {
        return content;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public String getPromotionStartDate() {
        return promotionStartDate;
    }

    public String getPromotionEndDate() {
        return promotionEndDate;
    }

    public String getOrderDiscount() {
        return orderDiscount;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public String getPromotionProductDiscount() {
        return promotionProductDiscount;
    }

    public String getPromotionProductDiscountName() {
        return promotionProductDiscountName;
    }

    public String getPromotionBuyName() {
        return promotionBuyName;
    }

    public String getPromotionGetName() {
        return promotionGetName;
    }

    public String getPromotionBuyQuantity() {
        return promotionBuyQuantity;
    }

    public String getPromotionGetQuantity() {
        return promotionGetQuantity;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("promotionName",promotionName);
        result.put("promotionStartDate",promotionStartDate);
        result.put("promotionEndDate",promotionEndDate);

        return result;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public String getPromotionGetCode() {
        return promotionGetCode;
    }
}
