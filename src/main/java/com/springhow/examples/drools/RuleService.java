package com.springhow.examples.drools;

import com.springhow.examples.drools.domain.OrderRequest;
import org.springframework.stereotype.Service;


public interface RuleService {
     OrderRequest getDiscount(OrderRequest req);
     void reload();
}
