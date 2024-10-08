package com.hg.spring.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: Đánh dấu lớp này là một Spring MVC REST controller. Nó cho phép các phương thức trong lớp trả về dữ liệu trực tiếp (thường là JSON hoặc XML) thay vì một view
@RestController
@RequestMapping("/batchs")
public class BatchCustomerController {

	// Tiêm (inject) đối tượng JobLauncher vào controller. JobLauncher là thành phần chịu trách nhiệm khởi chạy một batch job trong Spring Batch.
	@Autowired
	private JobLauncher jobLauncher;
	
	// Tiêm (inject) đối tượng Job, đại diện cho batch job mà bạn đã cấu hình trong ứng dụng. Job này đã được định nghĩa trước đó (chẳng hạn qua một bean) và bao gồm các bước (steps) cần thiết.
	@Autowired
	private Job job;
	
	@PostMapping("/importCustomers")
	public void importCsvToDB() {
		// Dùng để tạo ra một tập các job parameters (tham số) cần thiết để khởi chạy job. Trong trường hợp này, một tham số duy nhất startAt được thêm vào, chứa thời gian hiện tại (thông qua System.currentTimeMillis()).
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis()).toJobParameters();

		try {
			// Gọi phương thức run() của JobLauncher để khởi chạy batch job được chỉ định (đối tượng job) với các tham số đi kèm (jobParameters).
			// Job đã được cấu hình với các bước (step) trước đó, chẳng hạn như đọc dữ liệu từ CSV, xử lý dữ liệu, và ghi vào cơ sở dữ liệu.
			jobLauncher.run(job, jobParameters);
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			// JobExecutionAlreadyRunningException: Xảy ra nếu job đã đang chạy.
			// JobRestartException: Xảy ra nếu job không thể khởi động lại.
			// JobInstanceAlreadyCompleteException: Xảy ra nếu job đã hoàn thành trước đó với cùng một bộ job parameters.
			// JobParametersInvalidException: Xảy ra nếu các job parameters không hợp lệ.
			e.printStackTrace();
		}

	}
}
