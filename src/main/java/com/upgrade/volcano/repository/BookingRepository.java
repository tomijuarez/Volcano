package com.upgrade.volcano.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	public List<Booking> findByDateGreaterThanEqualAndDateLessThanEqual(LocalDate from, LocalDate to);
	
	public List<Booking> findByUuid(String uuid);
	
	@Modifying(clearAutomatically = true)
	@Query(value = "update Booking r set r.date =:date, r.date_to =:date where r.id =:id", nativeQuery = true)
	public void changeDates(@Param("date") LocalDate date, @Param("id") long id);
}
