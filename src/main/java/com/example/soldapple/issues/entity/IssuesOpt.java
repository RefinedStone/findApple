package com.example.soldapple.issues.entity;

import com.example.soldapple.create_price.dto.GetIPhonePriceResDto;
import com.example.soldapple.create_price.dto.GetMacbookPriceResDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class IssuesOpt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    private String category;
    private Integer years;
    private String model;
    private String options;
    private Integer batteryState;
    private String state;
    private String careOX;
    private String careDate;
    private Integer getPrice;
    private String ram;
    private String keyboard;
    private String storage;

    @OneToOne
    @JoinColumn(name = "issuesId")
    private Issues issues;

    public IssuesOpt(GetMacbookPriceResDto macbookOption, Issues issues) {
        this.category = macbookOption.getCategory();
        this.years = macbookOption.getYears();
        this.model = macbookOption.getModel();
        this.options = macbookOption.getOptions();
        this.ram = macbookOption.getRam();
        this.keyboard = macbookOption.getKeyboard();
        this.storage = macbookOption.getStorage();
        this.batteryState = macbookOption.getBatteryState();
        this.state = macbookOption.getMacbookState();
        this.careOX = macbookOption.getCareOX();
        this.careDate = macbookOption.getCareDate();
        this.getPrice = macbookOption.getGetPrice();
        this.issues = issues;
    }

    public IssuesOpt(GetIPhonePriceResDto iphoneOption, Issues issues) {
        this.category=iphoneOption.getCategory();
        this.years=iphoneOption.getYears();
        this.model=iphoneOption.getModel();
        this.options=iphoneOption.getOptions();
        this.batteryState = iphoneOption.getBatteryState();
        this.state = iphoneOption.getIphoneState();
        this.careOX = iphoneOption.getCareOX();
        this.careDate = iphoneOption.getCareDate();
        this.getPrice = iphoneOption.getGetPrice();
        this.issues = issues;
    }
}
