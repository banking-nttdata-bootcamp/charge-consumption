package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.ChargeConsumption;

public interface KafkaService {
    void publish(ChargeConsumption chargeConsumption);
}
