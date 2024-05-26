package com.megapro.invoicesync.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.megapro.invoicesync.dto.TaxMapper;
import com.megapro.invoicesync.dto.request.CountTaxRequestDTO;
import com.megapro.invoicesync.dto.response.CountTaxResponseDTO;
import com.megapro.invoicesync.dto.response.ReadTaxResponseDTO;
import com.megapro.invoicesync.repository.UserAppDb;
import com.megapro.invoicesync.service.TaxService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TaxRestController {
    @Autowired
    TaxService taxService;

    @Autowired
    TaxMapper taxMapper;

    @Autowired
    UserAppDb userAppDb;

    // @PostMapping("/count-tax")
    // public String countTax(CountTaxRequestDTO countRequest, Model model){
    //     var taxType = taxService.findById(countRequest.getId()).getTaxType();
    //     if(taxType.equals("include")){
    //         var countTaxResponse = taxService.countIncludedTaxes(countRequest);
    //         model.addAttribute("countResponse", countTaxResponse);
    //     } else {
    //         var countTaxResponse = taxService.countExcludedTaxes(countRequest);
    //         model.addAttribute("countResponse", countTaxResponse);
    //     }

    //     // TODO
    //     return "";
    // }

    @GetMapping("/api/taxes")
    public ResponseEntity<List<ReadTaxResponseDTO>> getTaxes() {
        var taxes = taxService.getTaxes();
        var taxesDTO = taxMapper.taxListToReadTaxResponseDTOList(taxes);
        System.out.println("Ini taxesnya " + taxesDTO);
        return ResponseEntity.ok(taxesDTO);
    }
    

    @PostMapping("/api/count-tax")
    public ResponseEntity<CountTaxResponseDTO> countTaxAPI(@RequestBody CountTaxRequestDTO countRequest){
        System.out.println("Ini tax request");
        System.out.println(countRequest);
        var countTaxResponse = taxService.countTax(countRequest);
        return ResponseEntity.ok(countTaxResponse);
    }
    
}

