package com.hg.spring.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.hg.spring.entity.Customer;
import com.hg.spring.repository.CustomerRepository;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {

	@Autowired
	private CustomerRepository customerRepository;
	
	// @Bean: Annotation này khai báo rằng phương thức reader sẽ trả về một bean được quản lý bởi Spring. Bean này có thể được sử dụng ở các nơi khác trong ứng dụng.
	@Bean
	public FlatFileItemReader<Customer> reader(){
		// Tạo một instance của FlatFileItemReader, một công cụ đọc tệp dòng phẳng (flat file) được cấu hình để ánh xạ các dòng từ tệp vào các đối tượng Customer.
		FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
		// Chỉ định tệp nguồn từ đó sẽ đọc dữ liệu
		itemReader.setResource(new FileSystemResource("src/main/resources/csv/customers.csv"));
		// Đặt tên cho reader này, có thể hữu ích khi theo dõi
		itemReader.setName("csvReader");
		// Bỏ qua dòng đầu tiên (thường là tiêu đề của tệp CSV)
		itemReader.setLinesToSkip(1);
		// Thiết lập LineMapper để ánh xạ từng dòng trong tệp thành đối tượng Customer
		itemReader.setLineMapper(lineMapper());
		return itemReader;
	}

	private LineMapper<Customer> lineMapper() {
		// lớp mặc định để ánh xạ dòng dữ liệu (từ tệp CSV) thành đối tượng Customer.
		DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
		// Tạo một tokenizer để chia các dòng dữ liệu dựa trên ký tự phân cách (delimiter).
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		// Thiết lập dấu phân cách (delimiter) cho tokenizer là dấu phẩy (,). Điều này có nghĩa là mỗi dòng trong tệp CSV sẽ được tách thành các phần tử khác nhau dựa trên dấu phẩy.
		lineTokenizer.setDelimiter(",");
		// Thiết lập chế độ strict là false. Điều này có nghĩa là nếu dòng nào thiếu cột, Spring Batch vẫn có thể tiếp tục xử lý mà không ném ngoại lệ.
		lineTokenizer.setStrict(false);
		// Xác định danh sách các tên cột (header) trong tệp CSV. Mỗi giá trị trong một dòng CSV sẽ được ánh xạ theo thứ tự với những tên cột này. Ví dụ: giá trị đầu tiên trong dòng sẽ tương ứng với id, giá trị thứ hai tương ứng với firstName, v.v.
		lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");
		// Tạo một đối tượng BeanWrapperFieldSetMapper để ánh xạ các giá trị từ CSV vào các thuộc tính của đối tượng Customer.
		BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		// Thiết lập kiểu dữ liệu đích cho fieldSetMapper là lớp Customer. Điều này có nghĩa là mỗi dòng CSV sẽ được chuyển thành một đối tượng Customer.
		fieldSetMapper.setTargetType(Customer.class);
		// Gán tokenizer đã cấu hình cho lineMapper, để nó biết cách tách các dòng dữ liệu từ tệp CSV.
		lineMapper.setLineTokenizer(lineTokenizer);
		// Gán field mapper đã cấu hình cho lineMapper, để ánh xạ các giá trị từ tokenizer vào các thuộc tính của đối tượng Customer.
		lineMapper.setFieldSetMapper(fieldSetMapper);
		
		return lineMapper;
	}
	
	// Phương thức này trả về một đối tượng CustomerProcessor, là lớp đã được bạn định nghĩa trước đó, triển khai giao diện ItemProcessor.
	@Bean
	public CustomerProcessor processor() {
		// Khởi tạo và trả về một đối tượng CustomerProcessor mới. Khi Spring Batch cần sử dụng bộ xử lý (processor) cho một batch job, nó sẽ sử dụng bean này.
		return new CustomerProcessor();
	}
	
	//Phương thức này trả về một đối tượng RepositoryItemWriter<Customer>. Đây là một lớp của Spring Batch để ghi dữ liệu theo kiểu item writer, sử dụng một repository để lưu trữ các đối tượng trong batch.
	@Bean
	public RepositoryItemWriter<Customer> writer(){
		// Khởi tạo một đối tượng RepositoryItemWriter với kiểu Customer, được dùng để ghi dữ liệu dạng từng mục (item writer) cho các đối tượng Customer.
		RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
		
		// Thiết lập repository cho RepositoryItemWriter.
		//  customerRepository là một đối tượng repository (thường là một JpaRepository hoặc CrudRepository) mà Spring Data JPA sẽ sử dụng để thao tác với cơ sở dữ liệu. customerRepository phải được tiêm (injected) vào để quản lý các đối tượng Customer trong cơ sở dữ liệu.
		writer.setRepository(customerRepository);
		// Thiết lập phương thức mà RepositoryItemWriter sẽ gọi trên repository để ghi dữ liệu. 
		// phương thức save() được chỉ định, nghĩa là RepositoryItemWriter sẽ gọi phương thức save() của repository để lưu các đối tượng Customer vào cơ sở dữ liệu. 
		writer.setMethodName("save");
		return writer;
	}
	
	/* 
	 * Step: Đây là một thành phần của Spring Batch, đại diện cho một bước xử lý trong quy trình batch. Mỗi Step có thể bao gồm các bước con như đọc (reader), xử lý (processor), và ghi (writer).
	 * JobRepository quản lý thông tin trạng thái của các job và step trong Spring Batch, bao gồm lưu trữ thông tin về trạng thái thực thi của batch job (đã bắt đầu, đã kết thúc, thất bại, thành công,...).
	 * PlatformTransactionManager : Đây là quản lý giao dịch (transaction manager), giúp đảm bảo rằng các bước trong batch job được thực hiện trong một giao dịch, nghĩa là nếu có lỗi, mọi thao tác đọc, xử lý, hoặc ghi đều có thể được hoàn tác (rollback).
	 */
	@Bean
	public Step step1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		// StepBuilder("csv-step", jobRepository): Đây là một builder để tạo Step. "csv-step" là tên của step, và jobRepository được truyền vào để lưu trữ và quản lý thông tin liên quan đến step này.
		return new StepBuilder("csv-step", jobRepository)
				// chunk(10, platformTransactionManager): Định nghĩa kích thước của "chunk" là 10. Điều này có nghĩa là sau mỗi lần xử lý 10 đối tượng Customer, hệ thống sẽ thực hiện ghi vào cơ sở dữ liệu. Việc xử lý theo chunk giúp giảm tải và nâng cao hiệu suất xử lý.
				.<Customer,Customer>chunk(10, platformTransactionManager)
				// Cấu hình các thành phần reader, processor và writer đã được định nghĩa trước đó để đọc, xử lý và ghi dữ liệu.
				.reader(reader())
				.processor(processor())
				.writer(writer())
				// Sử dụng TaskExecutor để thực hiện các tác vụ không đồng bộ. TaskExecutor được định nghĩa trước đó (sử dụng SimpleAsyncTaskExecutor) và giới hạn số lượng luồng đồng thời là 10. Điều này cho phép xử lý song song nhiều chunk, nâng cao hiệu suất xử lý.
				.taskExecutor(taskExecutor())
				// Hoàn tất việc xây dựng step và trả về đối tượng Step.
				.build();
	}
	
	// Job: Một Job trong Spring Batch đại diện cho toàn bộ quy trình xử lý batch, bao gồm nhiều Step.
	@Bean
	public Job runJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		/*
		 * JobBuilder("importCustomers", jobRepository): Sử dụng JobBuilder để tạo một job mới với tên là "importCustomers", và jobRepository sẽ quản lý thông tin về trạng thái của job này.
		 * .flow(step1(jobRepository, platformTransactionManager)): Tạo ra một luồng (flow) với step đầu tiên là step1 (đã định nghĩa ở trên). flow chỉ định trình tự thực hiện của các step.
		 * .end(): Kết thúc cấu hình của job. Ở đây, job chỉ có một step nên job sẽ kết thúc sau khi step1 hoàn thành.
		 * .build(): Hoàn thành việc xây dựng job và trả về đối tượng Job.
		 */
		return new JobBuilder("importCustomers", jobRepository)
				.flow(step1(jobRepository, platformTransactionManager)).end().build();
	}
	
	// TaskExecutor là một interface của Spring cho phép thực thi các tác vụ (tasks) trong một ứng dụng theo cách không đồng bộ.
	@Bean
	public TaskExecutor taskExecutor() {
		// SimpleAsyncTaskExecutor là một trong những triển khai đơn giản của TaskExecutor. Nó không thực sự quản lý một pool (bộ luồng) các luồng mà mỗi lần nó nhận được một task, nó sẽ tạo ra một luồng mới để thực thi task đó.
		// Đây không phải là lựa chọn tối ưu cho các ứng dụng đòi hỏi hiệu suất cao vì nó không tái sử dụng các luồng đã tạo, nhưng thích hợp cho các tình huống đơn giản hoặc khi không cần quá nhiều kiểm soát về luồng.
		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
		// Giới hạn số lượng luồng có thể chạy đồng thời ở mức 10. Điều này có nghĩa là sẽ có tối đa 10 luồng chạy đồng thời; nếu có hơn 10 tác vụ được gửi đến SimpleAsyncTaskExecutor, các tác vụ sẽ bị tạm dừng cho đến khi có một luồng trống.
		asyncTaskExecutor.setConcurrencyLimit(10);
		// Đối tượng SimpleAsyncTaskExecutor được trả về và sẽ được Spring quản lý như một bean. Khi các thành phần khác trong ứng dụng cần sử dụng một TaskExecutor, Spring sẽ tiêm (inject) đối tượng này vào.
		return asyncTaskExecutor;
	}
}
