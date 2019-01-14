package com.upgrade.volcano.resources;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.volcano.repository.BookingRepository;
import com.upgrade.volcano.repository.Booking;

@RestController
public class BookingResource {

	private BookingRepository bookingRepository;
	
	public BookingResource(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}
	
	private ResponseEntity<BookingResourceResponse> buildResponse(HttpStatus status, BookingResourceResponse body) {
		return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
	}
	
	private ResponseEntity<BookingResourceResponse> validateDates(LocalDate from, LocalDate to) {
		Long difference = ChronoUnit.DAYS.between(LocalDate.now(), to);
		
		if (difference < 1L || difference > 30L) {
			return buildResponse(HttpStatus.BAD_REQUEST, new BookingResourceResponse("the campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance."));
		}
		
		difference = ChronoUnit.DAYS.between(from, to);
		
		if (difference < 0 || difference > 2L) {
			return buildResponse(HttpStatus.BAD_REQUEST, new BookingResourceResponse("the campsite can be reserved for max 3 days and at least for 1 day."));
		}
		
		return null;
	}
	
	@GetMapping("/booking")
	public ResponseEntity<BookingResourceResponse> getAllBookings(
		@RequestParam(value="from", required=false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate from,
		@RequestParam(value="to", required=false) 
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate to
	) {
		from = (from == null) ? LocalDate.now() : from;
		to = (to == null) ? LocalDate.now().plusMonths(1) : to;
		
		return buildResponse(HttpStatus.OK, new BookingResourceResponse(bookingRepository.findByDateGreaterThanEqualAndDateLessThanEqual(from, to)));
	}
	
	@GetMapping("/booking/{id}")
	public ResponseEntity<BookingResourceResponse> getAllBookings(@PathVariable String id) {
		List<Booking> reservations = bookingRepository.findByUuid(id);
		return (reservations != null && reservations.size() > 0)
				? buildResponse(HttpStatus.OK, new BookingResourceResponse(reservations))
				: buildResponse(HttpStatus.NOT_FOUND, new BookingResourceResponse("the booking you were looking for doesn't exist"));
	}

	
	@PostMapping("/booking")
	@Transactional
	public ResponseEntity<BookingResourceResponse> createBooking(@RequestBody Booking body) {
		ResponseEntity<BookingResourceResponse> response = validateDates(body.getDate(), body.getDateTo());
		
		if (response != null) //it contains some error response.
			return response;
		
		//the validation has returned null, so our dates are good to go.
		Long difference = ChronoUnit.DAYS.between(body.getDate(), body.getDateTo());

		String uuid = UUID.randomUUID().toString();
		body.setUuid(uuid);
		
		try {
			int day = 0;
			
			do {
				bookingRepository.save(new Booking(body.getName(), body.getLastName(), body.getEmail(), body.getDate().plusDays(day), uuid));
				day++;
			}
			while(day <= difference);
			
			return buildResponse(HttpStatus.CREATED, new BookingResourceResponse(body));
		}
		catch(Exception e) {
			throw new ConflictException();
		}
	}
	
	@PatchMapping("/booking/{id}")
	@Transactional
	public ResponseEntity<BookingResourceResponse> updateBooking(@RequestBody Booking body, @PathVariable String id) {
		if (body.getDate() == null || body.getDateTo() == null)
			return buildResponse(HttpStatus.BAD_REQUEST, new BookingResourceResponse("both start and end dates are required to update the booking date."));

		ResponseEntity<BookingResourceResponse> response = validateDates(body.getDate(), body.getDateTo());
		
		if (response != null) //it contains some error response.
			return response;
		
		//the validation has returned null, so our dates are good to go.
		List<Booking> reservations = bookingRepository.findByUuid(id);
		
		if (reservations != null && reservations.size() == 0)
			return buildResponse(HttpStatus.NOT_FOUND, new BookingResourceResponse("the booking you were trying to update doesn't exist.;"));

		//For simplicity sake let's assume updates can only be within bookings that have the same length
		long difference = ChronoUnit.DAYS.between(body.getDate(), body.getDateTo());
		if (reservations != null && reservations.size() != difference + 1) //+1 because we count one day for the first
			return buildResponse(HttpStatus.BAD_REQUEST, new BookingResourceResponse("the booking you were trying to update should have the same days as your new booking.;"));
		
		try {
			int day = 0;
			for(Booking reservation: reservations) {
				bookingRepository.changeDates(body.getDate().plusDays(day), reservation.getId());
				day++;
			}
			
			return buildResponse(HttpStatus.NO_CONTENT, null);
		}
		catch (Exception e) {
			throw new InternalServerErrorException();
		}				
	}
	
	@DeleteMapping("/booking/{id}")
	@Transactional
	public ResponseEntity<BookingResourceResponse> deleteBooking(@PathVariable String id) {
		List<Booking> items = bookingRepository.findByUuid(id);
		
		try {
			items.forEach(item -> bookingRepository.delete(item));
		}
		catch (Exception e) {
			throw new InternalServerErrorException();
		}
		
		if (items != null && items.size() == 0)
			return buildResponse(HttpStatus.NOT_FOUND, new BookingResourceResponse("there's no booking with such UUID."));
		
		else return buildResponse(HttpStatus.NO_CONTENT, null);

	}
}
