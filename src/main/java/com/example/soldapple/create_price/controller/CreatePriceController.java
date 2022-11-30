package com.example.soldapple.create_price.controller;

import com.example.soldapple.create_price.dto.*;
import com.example.soldapple.create_price.service.CreatePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class CreatePriceController {
    private final CreatePriceService createPriceService;

    @PostMapping("/iphone")
    public GetIPhonePriceResDto getIPhonePrice(@RequestBody GetIPhonePriceReqDto getIPhonePriceResDto){
        return createPriceService.getIPhonePrice(getIPhonePriceResDto);
    }

    @PostMapping("/macbook")
    public GetMacbookPriceResDto getMacbookPrice(@RequestBody GetMacbookPriceReqDto getMacbookPriceReqDto){
        return createPriceService.getMacbookPrice(getMacbookPriceReqDto);
    }

    @GetMapping("/iphone")  //iPhone,Macbook
    public List<Integer> iphoneFirst() {
        return createPriceService.iphoneFirst();
    }

    @GetMapping("/iphone/{year}")   //출시년도
    public List<String> iphoneSecond(@PathVariable Integer year) {
        return createPriceService.iphoneSecond(year);
    }

    @GetMapping("/iphone/{year}/{model}")    //기종
    public List<String> iphoneThird(@PathVariable Integer year,
                              @PathVariable String model) {
        return createPriceService.iphoneThird(year, model);
    }

    @GetMapping("/macbook")  //iPhone,Macbook
    public List<Integer> macbookFirst() {
        return createPriceService.macbookFirst();
    }

    @GetMapping("/macbook/{year}")   //출시년도
    public List<String> macbookSecond(@PathVariable Integer year) {
        return createPriceService.macbookSecond(year);
    }
    @GetMapping("/macbook/{year}/{model}-{cpu}")    //기종
    public List<Integer> macbookThird(@PathVariable Integer year,
                                @PathVariable String model,
                                @PathVariable String cpu) {
        return createPriceService.macbookThird(year, model, cpu);
    }

    @GetMapping("/macbook/{year}/{model}-{cpu}/{inch}")    //기종
    public MacbookResDto macbookFourth(@PathVariable Integer year,
                                       @PathVariable String model,
                                       @PathVariable String cpu,
                                       @PathVariable Integer inch) {
        return createPriceService.macbookFourth(year, model, cpu, inch);
    }
}
