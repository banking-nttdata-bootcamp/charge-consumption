package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import com.nttdata.bootcamp.repository.ChargeConsumptionRepository;
import com.nttdata.bootcamp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

//Service implementation
@Service
public class ChargeConsumptionServiceImpl implements ChargeConsumptionService {
    @Autowired
    private ChargeConsumptionRepository chargeConsumptionRepository;

    @Override
    public Flux<ChargeConsumption> findAll() {
        Flux<ChargeConsumption> chargeConsumptionFlux = chargeConsumptionRepository.findAll();
        return chargeConsumptionFlux;
    }

    @Override
    public Flux<ChargeConsumption> findByAccountNumber(String accountNumber) {
        Flux<ChargeConsumption> chargeConsumptionFlux = chargeConsumptionRepository
                .findAll()
                .filter(x -> x.getAccountNumber().equals(accountNumber));
        return chargeConsumptionFlux;
    }

    @Override
    public Mono<ChargeConsumption> findByNumber(String Number) {
        Mono<ChargeConsumption> loadBalanceMono = chargeConsumptionRepository
                .findAll()
                .filter(x -> x.getChargeNumber().equals(Number))
                .next();
        return loadBalanceMono;
    }

    @Override
    public Mono<ChargeConsumption> saveChargeConsumption(ChargeConsumption dataChargeConsumption) {
        Mono<ChargeConsumption> chargeConsumptionMono = findByNumber(dataChargeConsumption.getDni())
                .flatMap(__ -> Mono.<ChargeConsumption>error(new Error("This charge number" + dataChargeConsumption.getDni() + " exists")))
                .switchIfEmpty(chargeConsumptionRepository.save(dataChargeConsumption));
        return chargeConsumptionMono;


    }

    @Override
    public Mono<ChargeConsumption> updateChargeConsumption(ChargeConsumption dataChargeConsumption) {

        Mono<ChargeConsumption> transactionMono = findByNumber(dataChargeConsumption.getChargeNumber());
        try {
            dataChargeConsumption.setDni(transactionMono.block().getDni());
            dataChargeConsumption.setAmount(transactionMono.block().getAmount());
            dataChargeConsumption.setCreationDate(transactionMono.block().getCreationDate());
            return chargeConsumptionRepository.save(dataChargeConsumption);
        }catch (Exception e){
            return Mono.<ChargeConsumption>error(new Error("This charge consumption  " + dataChargeConsumption.getAccountNumber() + " do not exists"));
        }
    }

    @Override
    public Mono<Void> deleteChargeConsumption(String Number) {
        Mono<ChargeConsumption> loadBalanceMono = findByNumber(Number);
        try {
            ChargeConsumption chargeConsumption = loadBalanceMono.block();
            return chargeConsumptionRepository.delete(chargeConsumption);
        }
        catch (Exception e){
            return Mono.<Void>error(new Error("This charge consumption" + Number+ " do not exists"));
        }
    }





}
