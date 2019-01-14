package com.upgrade.volcano.resources;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.upgrade.volcano.repository.Booking;

public class BookingResourceResponse {
	
	private static class CustomResponse {
		private String uuid;
		private LocalDate from;
		private LocalDate to;
		
		CustomResponse (String uuid, LocalDate from, LocalDate to) {
			this.uuid = uuid;
			this.from = from;
			this.to = to;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public LocalDate getFrom() {
			return from;
		}

		public void setFrom(LocalDate from) {
			this.from = from;
		}

		public LocalDate getTo() {
			return to;
		}

		public void setTo(LocalDate to) {
			this.to = to;
		}

	}
	
	private String error;
	private Object payload;
	
	public BookingResourceResponse() {
	
	}
	
	BookingResourceResponse(String error) {
		this.error = error;
	}
	
	BookingResourceResponse(Booking payload) {
		this.payload = new CustomResponse(payload.getUuid(), payload.getDate(), payload.getDateTo());
	}
	
	BookingResourceResponse(List<Booking> payload) {
		this.payload = payload
						.stream()
						.map(book -> new CustomResponse(book.getUuid(), book.getDate(), book.getDateTo()))
						.collect(Collectors.toList());
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Booking payload) {
		this.payload = new CustomResponse(payload.getUuid(), payload.getDate(), payload.getDateTo());
	}
	
}
