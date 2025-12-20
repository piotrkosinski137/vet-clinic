/**
 * Patient module - manages animals/pets in the veterinary clinic.
 *
 * <h2>Module Structure (Hexagonal Architecture)</h2>
 *
 * <ul>
 *   <li>{@code api} - Inbound adapters (REST controllers, DTOs)
 *   <li>{@code domain} - Core business logic (entities, services, ports)
 *   <li>{@code infrastructure} - Outbound adapters (repositories, external clients)
 * </ul>
 *
 * <h2>Public API</h2>
 *
 * Other modules should ONLY depend on:
 *
 * <ul>
 *   <li>{@link com.vetclinic.patient.PatientModuleConfig} - to import this module
 *   <li>Classes in {@code domain.model} package - for shared IDs/references
 * </ul>
 */
package com.vetclinic.patient;
