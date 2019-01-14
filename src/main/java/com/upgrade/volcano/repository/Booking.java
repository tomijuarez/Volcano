package com.upgrade.volcano.repository;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.GenerationType;

@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String uuid;
	private String email;
	private String name;
	@Column(name="last_name")
	private String lastName;
	@Column(unique=true)
	private LocalDate date;
	//Maybe we change this in the future to so it's worth to keep this value here. 
	// This could be useful for a few hours booking and not only daily bookings.	
	@Column(name="date_to")
	private LocalDate dateTo;
	
	public Booking() {
		
	}
	
	public Booking(String name, String lastName, String email, LocalDate date, String uuid) {
		this.name = name;
		this.lastName = lastName;
		this.email = email;
		this.date = date;
		this.dateTo = date; //Maybe we change this in the future so it's worth to keep this value here.
		this.uuid = uuid;
	}
	
	public Booking(String name, String lastName, String email, LocalDate date, LocalDate dateTo) {
		this.name = name;
		this.lastName = lastName;
		this.email = email;
		this.date = date;
		this.dateTo = dateTo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getId() {
		return id;
	}

	public void getId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public LocalDate getDateTo() {
		return dateTo;
	}

	public void setDateTo(LocalDate dateTo) {
		this.dateTo = dateTo;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
