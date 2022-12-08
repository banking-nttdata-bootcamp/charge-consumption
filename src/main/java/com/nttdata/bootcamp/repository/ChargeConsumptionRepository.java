package com.nttdata.bootcamp.repository;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

//Mongodb Repository
public interface ChargeConsumptionRepository extends ReactiveCrudRepository<ChargeConsumption, String> {
}
