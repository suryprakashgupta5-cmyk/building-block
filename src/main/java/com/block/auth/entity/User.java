package com.block.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user")
@Getter @Setter @NoArgsConstructor @SuperBuilder
public class User {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	 
	 @Column(name = "full_name")
	 private String fullName;
	 
	 @Column(name = "address")
	 private String address;
	 
	 @Column(name = "gmail")
	 private String gmail;
	 
	 @Column(name = "mobile_no")
	 private String mobileNo;


}
