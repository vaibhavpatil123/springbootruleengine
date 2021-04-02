package com.springhow.examples.drools;

import com.springhow.examples.drools.domain.OrderRequest;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DecisionController {

    private final RuleService ruleService;

    public DecisionController(RuleService ruleService) {
       this.ruleService = ruleService;
   }

    @PostMapping("/discount")
    private OrderRequest getDiscountPercent(@RequestBody OrderRequest orderRequest) {
        return ruleService.getDiscount(orderRequest);
    }
    @PostMapping("/reload")
    private void getDiscountPercent() {
        ruleService.reload();
    }
}
