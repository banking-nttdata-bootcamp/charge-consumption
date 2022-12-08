package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.ChargeConsumption;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nttdata.bootcamp.service.ChargeConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/chargeConsumption")
public class ChargeConsumptionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChargeConsumptionController.class);
	@Autowired
	private ChargeConsumptionService chargeConsumptionService;

	//charge consumption search
	@GetMapping("/findAllLoadBalance")
	public Flux<ChargeConsumption> findAllChargeConsumption() {
		Flux<ChargeConsumption> charges = chargeConsumptionService.findAll();
		LOGGER.info("Registered charge consumption: " + charges);
		return charges;
	}

	//charge consumption by AccountNumber
	@GetMapping("/findAllChargeConsumptionByAccountNumber/{accountNumber}")
	public Flux<ChargeConsumption> findAllChargeConsumptionByAccountNumber(@PathVariable("accountNumber") String accountNumber) {
		Flux<ChargeConsumption> charges = chargeConsumptionService.findByAccountNumber(accountNumber);
		LOGGER.info("Registered charge consumption of account number: "+accountNumber +"-" + charges);
		return charges;
	}

	//charge consumption  by Number
	@CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
	@GetMapping("/findByChargeConsumptionNumber/{numberDeposits}")
	public Mono<ChargeConsumption> findByChargeConsumptionNumber(@PathVariable("numberDeposits") String numberDeposits) {
		LOGGER.info("Searching charge consumption by number: " + numberDeposits);
		return chargeConsumptionService.findByNumber(numberDeposits);
	}

	//Save charge consumption
	@CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
	@PostMapping(value = "/saveChargeConsumption")
	public Mono<ChargeConsumption> saveChargeConsumption(@RequestBody ChargeConsumption dataChargeConsumption,
														 @PathVariable("creditLimit") Double creditLimit){
		ChargeConsumption datacharge = new ChargeConsumption();
		if(creditLimit>=dataChargeConsumption.getAmount()) {
			Mono.just(datacharge).doOnNext(t -> {
						t.setDni(dataChargeConsumption.getDni());
						t.setAmount(dataChargeConsumption.getAmount());
						t.setChargeNumber(dataChargeConsumption.getChargeNumber());
						t.setTypeAccount("active");
						t.setAccountNumber(dataChargeConsumption.getAccountNumber());
						t.setStatus("active");
						t.setCreationDate(new Date());
						t.setModificationDate(new Date());
					}).onErrorReturn(datacharge).onErrorResume(e -> Mono.just(datacharge))
					.onErrorMap(f -> new InterruptedException(f.getMessage())).subscribe(x -> LOGGER.info(x.toString()));
		}
		Mono<ChargeConsumption> newCharge = chargeConsumptionService.saveChargeConsumption(datacharge);
		return newCharge;
	}

	//Update charge consumption
	@CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
	@PutMapping("/updateChargeConsumption/{numberTransaction}")
	public Mono<ChargeConsumption> updateChargeConsumption(@PathVariable("numberTransaction") String numberTransaction,
													 @Valid @RequestBody ChargeConsumption dataChargeConsumption) {
		Mono.just(dataChargeConsumption).doOnNext(t -> {

					t.setChargeNumber(numberTransaction);
					t.setModificationDate(new Date());

				}).onErrorReturn(dataChargeConsumption).onErrorResume(e -> Mono.just(dataChargeConsumption))
				.onErrorMap(f -> new InterruptedException(f.getMessage())).subscribe(x -> LOGGER.info(x.toString()));

		Mono<ChargeConsumption> updateChargeConsumption = chargeConsumptionService.updateChargeConsumption(dataChargeConsumption);
		return updateChargeConsumption;
	}


	//Delete charge consumption
	@CircuitBreaker(name = "charge-consumption", fallbackMethod = "fallBackGetChargeConsumption")
	@DeleteMapping("/deleteChargeConsumption/{numberTransaction}")
	public Mono<Void> deleteChargeConsumption(@PathVariable("numberTransaction") String numberTransaction) {
		LOGGER.info("Deleting charge consumption by number: " + numberTransaction);
		Mono<Void> delete = chargeConsumptionService.deleteChargeConsumption(numberTransaction);
		return delete;

	}


	@GetMapping("/getCountChargeConsumption/{accountNumber}")
	//get count of LoadBalance
	public Mono<Long> getCountChargeConsumption(@PathVariable("accountNumber") String accountNumber){
		Flux<ChargeConsumption> transactions= findAllChargeConsumptionByAccountNumber(accountNumber);
		return transactions.count();
	}


	private Mono<ChargeConsumption> fallBackGetChargeConsumption(Exception e){
		ChargeConsumption chargeConsumption = new ChargeConsumption();
		Mono<ChargeConsumption> loadBalanceMono= Mono.just(chargeConsumption);
		return loadBalanceMono;
	}


}
