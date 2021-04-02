package com.springhow.examples.drools;

import com.springhow.examples.drools.domain.GrandOrderRequest;
import com.springhow.examples.drools.domain.OrderRequest;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RuleServiceImpl implements RuleService {
    KieServices kieServices;
    @Autowired
    private ResourceLoader resourceLoader;
    private Map<String ,KieSession> cacheContryKieSessions =new ConcurrentHashMap<>();
    public RuleServiceImpl(KieServices kieServices){
        this
                .kieServices=kieServices;
    }


    // this method reads all rules from spring configuration path and create per/country Kie
    // session on startup and store into Hashap
    @PostConstruct
    public void doInt() {
        try {

            // 1 read per country rule folder
            File parentDirectoty=resourceLoader.getResource("classpath:hk").getFile();
            // 2 read each rule file of contry
            if (parentDirectoty.isDirectory()) {
                List<Resource> resources1= Arrays.asList(
                        ResourceFactory.newClassPathResource("discount.drl"),
                                ResourceFactory.newClassPathResource("discount1.drl")
                );

                KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
                resources1.forEach(kieFileSystem::write);
                KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
                kieBuilder.buildAll();
                KieModule kieModule = kieBuilder.getKieModule();
                KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
                KieSession kieSession = kieContainer.newKieSession();
              // 3 create country session and sava into hashmap


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public OrderRequest getDiscount(OrderRequest req) {
        return getKieContainer(req);
    }

    // reload Rules using rest API to
    @Override
    public void reload() {

        //cache cleanup and remove country sessions
        cacheContryKieSessions.get("hk").dispose();
        cacheContryKieSessions.get("hk").destroy();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("discount3.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        cacheContryKieSessions.put("discount.drl",kieSession);
    }
    // load session for request  request has information about contry
    private OrderRequest getKieContainer(OrderRequest orderRequest) {

        Resource resource= ResourceFactory.newClassPathResource("release2/version1/hk");
        //check in cache is contry session avaliabe
        if(cacheContryKieSessions.containsKey(orderRequest.getRule())) {
            KieSession kieSession = cacheContryKieSessions.get(orderRequest.getRule());
            // case1 object type used in RULE 1
            OrderRequest grandOrderRequest=new OrderRequest();
            grandOrderRequest.setTotalPrice(11111);
            grandOrderRequest.setOrderId(1212);
            grandOrderRequest.setPaymentType("CARD");
            kieSession.insert(grandOrderRequest);
            // case1 object type used in RULE 1  and fire rules on session
            kieSession.fireAllRules();
            // case2 object type 2  used in RULE 2
            System.out.println("grandOrderRequest "+grandOrderRequest.getDiscount());
            GrandOrderRequest OrderRequest=new GrandOrderRequest();
            OrderRequest.setTotalPrice(11111);
            OrderRequest.setOrderId(1212);
            OrderRequest.setPaymentType("CASH");
            kieSession.insert(OrderRequest);
            kieSession.fireAllRules(10);
            // case2 object type 2  used in RULE 2 and fire rules on session
            System.out.println("OrderRequest "+OrderRequest.getDiscount());
        }

        return orderRequest;
    }
}
