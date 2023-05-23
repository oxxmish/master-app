/*
 * Authorization API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package ru.freemiumhosting.master.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.freemiumhosting.master.utils.enums.UserRole;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * User
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name ="users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_gen")
  @SequenceGenerator(name = "users_gen", sequenceName = "users_seq", allocationSize = 1)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "password")
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "user_role")
  private UserRole userRole = UserRole.USER;

  @Column(name = "created_date")
  private OffsetDateTime createdDate;

  @Column(name = "currentCpu")
  private Long currentCpu;

  @Column(name = "request_cpu")
  private Long requestCpu;

  @Column(name = "availible_cpu")
  private Long availibleCpu;

  @Column(name = "current_ram")
  private Long currentRam;

  @Column(name = "request_ram")
  private Long requestRam;

  @Column(name = "availible_ram")
  private Long availibleRam;

  @Column(name = "current_storage")
  private Long currentStorage;

  @Column(name = "request_storage")
  private Long requestStorage;

  @Column(name = "availible_storage")
  private Long availibleStorage;
}

