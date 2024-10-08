package com.hg.spring.config;

import org.springframework.batch.item.ItemProcessor;

import com.hg.spring.entity.Customer;

/*
 * Lớp CustomerProcessor được khai báo để triển khai giao diện ItemProcessor. Giao diện này có hai kiểu tham số:
 * Customer (input): là kiểu dữ liệu đầu vào cho processor.
 * Customer (output): là kiểu dữ liệu sau khi xử lý và sẽ được truyền tiếp cho writer.
 */
public class CustomerProcessor implements ItemProcessor<Customer, Customer>{

	// Phương thức process() nhận đầu vào là một đối tượng Customer và trả về một đối tượng Customer sau khi xử lý.
	@Override
	public Customer process(Customer customer) throws Exception {
		return customer;
	}

}
