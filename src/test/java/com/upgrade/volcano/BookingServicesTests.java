package com.upgrade.volcano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.upgrade.volcano.repository.Booking;
import com.upgrade.volcano.repository.BookingRepository;
import com.upgrade.volcano.resources.BookingResource;
import com.upgrade.volcano.resources.ConflictExceptionController;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookingServicesTests {

    private MockMvc mvc;
    
    @Mock
    private BookingRepository bookingRepository;
 
    @InjectMocks
    private BookingResource bookingController;
 
    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());
        mvc = MockMvcBuilders
        		.standaloneSetup(bookingController)
        		.setControllerAdvice(new ConflictExceptionController())
        		.build();
    }
    
    @Test
    public void shouldCreateNewBooking() throws Exception {
    	
    	JSONObject reservation = new JSONObject();
    	reservation.put("email", "tomasjuarez@gmail.com");
    	reservation.put("name", "Tomas");
    	reservation.put("lastName", "Juarez");
    	reservation.put("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
        	mvc.perform(
                post("/booking/")
                	.contentType(MediaType.APPLICATION_JSON)
                	.content(reservation.toString())
                )
        	.andReturn()
        	.getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }
    
    @Test
    public void shouldReportConflictGivenSameBookingDates() throws Exception {

    	Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
    		   .thenThrow(new RuntimeException());
         
    	JSONObject reservation = new JSONObject();
    	reservation.put("email", "tomasjuarez@gmail.com");
    	reservation.put("name", "Tomas");
    	reservation.put("lastName", "Juarez");
    	reservation.put("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(
            	post("/booking/")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(reservation.toString())
	        )
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }
    
    @Test
    public void shouldReturn404WhenUpdatingNonExistingBooking() throws Exception {
    	
    	JSONObject reservation = new JSONObject();
    	reservation.put("email", "tomasjuarez@gmail.com");
    	reservation.put("name", "Tomas");
    	reservation.put("lastName", "Juarez");
    	reservation.put("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(
            	patch("/booking/"+UUID.randomUUID().toString())
            		.contentType(MediaType.APPLICATION_JSON)
	                .content(reservation.toString())
            )
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
    
    
	//For simplicity sake let's assume updates can only be within bookings that have the same length
    //For example, if I create a 3 days booking and then I try to change it to be a 2 days booking, it should fail.
    @Test
    public void shouldRejectWhenUpdatingDifferentLengthBooking() throws Exception {
    
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(2), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(3), uuid));
    	
    	Mockito.when(bookingRepository.findByUuid(uuid))
		   	   .thenReturn(reservations);

    	JSONObject reservation = new JSONObject();
    	reservation.put("date", LocalDate.now().plusDays(6).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(
            	patch("/booking/"+uuid)
            		.contentType(MediaType.APPLICATION_JSON)
	                .content(reservation.toString())
            )
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
    
    @Test
    public void shouldRejectMoreThanThreeDaysBooking() throws Exception {

    	//No need to mock the database because it will fail earlier.
         
    	JSONObject reservation = new JSONObject();
    	reservation.put("email", "tomasjuarez@gmail.com");
    	reservation.put("name", "Tomas");
    	reservation.put("lastName", "Juarez");
    	reservation.put("date", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(4).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(
            	post("/booking/")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(reservation.toString())
	        )
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
    
    @Test
    public void shouldUpdateBooking() throws Exception {
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(2), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(3), uuid));
    	
    	Mockito.when(bookingRepository.findByUuid(uuid))
		   	   .thenReturn(reservations);

    	JSONObject reservation = new JSONObject();
    	reservation.put("date", LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	reservation.put("dateTo", LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(
            	patch("/booking/"+uuid)
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(reservation.toString())
	        )
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
    
    @Test
    public void shouldReturnOneBooking() throws Exception {
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	
    	Mockito.when(bookingRepository.findByUuid(uuid))
		   	   .thenReturn(reservations);
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(get("/booking/"+uuid))
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        
        int length = JsonPath.parse(response.getContentAsString()).read("$.payload.length()");
        assertThat(length).isEqualTo(1);
    }
    
    @Test
    public void shouldReturnThreeDaysBooking() throws Exception {
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(2), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(3), uuid));    	
    	
    	Mockito.when(bookingRepository.findByUuid(uuid))
		   	   .thenReturn(reservations);
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(get("/booking/"+uuid))
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        
        int length = JsonPath.parse(response.getContentAsString()).read("$.payload.length()");
        assertThat(length).isEqualTo(3);
    }
    
    @Test
    public void shouldReturn404WhenSearchingForNonExistingBooking() throws Exception {

        // when
        MockHttpServletResponse response = 
            mvc.perform(get("/booking/"+UUID.randomUUID().toString()))
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
    
    @Test
    public void shouldReturnDefaultRangeOfDaysForBookingsList() throws Exception {
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(2), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(3), uuid));
    	reservations.add(new Booking("chiri@gmail.com", "Agustin", "Chirichigno", LocalDate.now().plusDays(15), UUID.randomUUID().toString())); 
    	reservations.add(new Booking("rochi@gmail.com", "Rocio", "Zalla", LocalDate.now().plusDays(19), UUID.randomUUID().toString())); 

    	
    	Mockito.when(bookingRepository.findByDateGreaterThanEqualAndDateLessThanEqual(LocalDate.now(), LocalDate.now().plusMonths(1)))
		   	   .thenReturn(reservations);
    	
        // when
        MockHttpServletResponse response = mvc.perform(get("/booking/")).andReturn().getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        
        int length = JsonPath.parse(response.getContentAsString()).read("$.payload.length()");
        assertThat(length).isEqualTo(5);
    }
    
    @Test
    public void shouldDeleteBooking() throws Exception {
    	List<Booking> reservations = new ArrayList<>();
		String uuid = UUID.randomUUID().toString();
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(1), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(2), uuid));
    	reservations.add(new Booking("tomasjuarez@gmail.com", "Tomas", "Juarez", LocalDate.now().plusDays(3), uuid));    	
    	
    	Mockito.when(bookingRepository.findByUuid(uuid))
		   	   .thenReturn(reservations);
    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(delete("/booking/"+uuid))
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
    
    @Test
    public void shouldResponse404WhenDeletingNonExistingBooking() throws Exception {    	
        // when
        MockHttpServletResponse response = 
            mvc.perform(delete("/booking/"+UUID.randomUUID().toString()))
            .andReturn()
            .getResponse();
        
        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

}
